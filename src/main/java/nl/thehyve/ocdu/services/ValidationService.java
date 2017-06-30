/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.RegisteredEventInformation;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.MissingEventError;
import nl.thehyve.ocdu.models.errors.StudyDoesNotExist;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import nl.thehyve.ocdu.repositories.EventRepository;
import nl.thehyve.ocdu.repositories.SubjectRepository;
import nl.thehyve.ocdu.validators.*;
import nl.thehyve.ocdu.validators.fileValidators.DataPreMappingValidator;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service performing OpenClinica consistency checking. Supports checking data, subjects and events for
 * consistency against targeted study/site. All methods here depend on OpenClinica Web-Services and therefore
 * require valid OcEnvironment (reachable and working OC 3.6 Server with SOAP-ws installed and configured)
 * and need valid OC User and sha1 hash of their password. All methods of this service accept UploadSession
 * (aka User Submission) as input - this object represents submission user made along with all submitted data.
 * This object is used to retrieve saved data/subjects/events and validate them.
 *
 * Created by piotrzakrzewski on 01/05/16.
 */
@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    DataService dataService;

    @Autowired
    MetaDataService metaDataService;

    /**
     * Returns errors in consistency against OpenClinica study definition (metadata) in user submitted data.
     *
     * @param submission
     * @param wsPwdHash
     * @return
     * @throws Exception
     */
    public List<ValidationErrorMessage> getDataErrors(UploadSession submission, String wsPwdHash, MetaDataProvider metaDataProvider) throws Exception {
        List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(submission);
        determineStudy(bySubmission, submission);
        OcUser submitter = submission.getOwner();
        Study study = dataService.findStudy(submission.getStudy(), submitter, wsPwdHash);
        MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, submitter, wsPwdHash, submission);

        List<StudySubjectWithEventsType> subjectWithEventsTypes = openClinicaService
                .getStudySubjectsType(submitter.getUsername(), wsPwdHash, submitter.getOcEnvironment(), study.getIdentifier(), "");
        List<ValidationErrorMessage> errors = new ArrayList<>();
        if (study == null || metadata == null) {
            StudyDoesNotExist studyError = new StudyDoesNotExist();
            studyError.addOffendingValue(submission.getStudy());
            errors.add(studyError);

            UtilChecks.addErrorClassificationToAll(bySubmission,ErrorClassification.BLOCK_ENTIRE_UPLOAD);
        } else {
            ClinicalDataChecksRunner checksRunner = new ClinicalDataOcChecks(metadata, bySubmission, subjectWithEventsTypes);
            errors.addAll(checksRunner.getErrors());
        }
        clinicalDataRepository.save(bySubmission);
        return errors;
    }

    public ValidationErrorMessage checkForMissingEventsInEventDataAndOpenClinica(MetaData metaData, List<StudySubjectWithEventsType> subjectWithEventsTypeList, List<Event> eventList, List<ClinicalData> clinicalDataList) {
        // public visibility for unit-testing
        Set<String> eventsPresentInOpenClinica = RegisteredEventInformation.createEventKeyListFromStudySubjectWithEventsTypeList(metaData, subjectWithEventsTypeList);
        List<String> eventsPresentInEventData = RegisteredEventInformation.createEventKeyListFromEventList(eventList);
        Set<String> eventsPresentInClinicalData = RegisteredEventInformation.createEventKeyListFromClinicalData(clinicalDataList);

        eventsPresentInClinicalData.removeAll(eventsPresentInEventData);
        eventsPresentInClinicalData.removeAll(eventsPresentInOpenClinica);
        if (eventsPresentInClinicalData.isEmpty()) {
            return null;
        }
        MissingEventError missingEventError = new MissingEventError();
        for (String offendingValue : eventsPresentInClinicalData) {
            String[] partList = StringUtils.splitPreserveAllTokens(offendingValue, ClinicalData.KEY_SEPARATOR);
            String errorMessage = "Subject: " + partList[2] + ", event: " + partList[3];
            if ((partList.length == 5) && StringUtils.isNotEmpty(partList[4])) {
                errorMessage += ", repeat number: " + partList[4];
            }
            errorMessage +=  ".";
            missingEventError.addOffendingValue(errorMessage);
        }

        return missingEventError;
    }
    /**
     * Returns errors in consistency against OpenClinica study definition (metadata) in event registration form
     * subitted by the user.
     *
     * @param submission
     * @param wsPwdHash
     * @return
     * @throws Exception
     */
    public List<ValidationErrorMessage> getEventsErrors(UploadSession submission, String wsPwdHash, MetaDataProvider metaDataProvider, String userName, String url) throws Exception {
        List<Event> events = eventRepository.findBySubmission(submission);
        List<Subject> subjectList = subjectRepository.findBySubmission(submission);
        OcUser submitter = submission.getOwner();
        Study study = dataService.findStudy(submission.getStudy(), submitter, wsPwdHash);
        MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, submitter, wsPwdHash, submission);
        events.forEach(event -> event.setStudyProtocolName(metadata.getProtocolName())); //TODO: Refactor setting studyProtocolName out of validation , this is not the right place to do it
        eventRepository.save(events);

        List<StudySubjectWithEventsType> studySubjectWithEventsTypeList =
                openClinicaService.getStudySubjectsType(userName, wsPwdHash, url, study.getIdentifier(), "");

        EventDataOcChecks checks = new EventDataOcChecks(metadata, events, subjectList, studySubjectWithEventsTypeList);
        List<ClinicalData> clinicalDataList = clinicalDataRepository.findBySubmission(submission);
        List<StudySubjectWithEventsType> subjectWithEventsTypeList =
                openClinicaService.getStudySubjectsType(submitter.getUsername(), wsPwdHash, submitter.getOcEnvironment(), study.getIdentifier(), "");


        List<ValidationErrorMessage> validationErrorMessages = checks.getErrors();

        ValidationErrorMessage missingEventsError = checkForMissingEventsInEventDataAndOpenClinica(metadata, subjectWithEventsTypeList, events, clinicalDataList);
        if (missingEventsError != null) {
            validationErrorMessages.add(missingEventsError);
        }
        eventRepository.save(events);
        return validationErrorMessages;
    }

    /**
     *
     * Returns errors in consistency against OpenClinica study definition (metadata) in user submitted subject
     * registration form.
     *
     * @param submission
     * @param wsPwdHash
     * @return
     * @throws Exception
     */
    public List<ValidationErrorMessage> getPatientsErrors(UploadSession submission, String wsPwdHash, MetaDataProvider metaDataProvider) throws Exception {
        List<Subject> bySubmission = subjectRepository.findBySubmission(submission);
        Set<String> subjectsInData = clinicalDataRepository.findBySubmission(submission)
                .stream().map(ClinicalData::getSsid).collect(Collectors.toSet());
        OcUser submitter = submission.getOwner();
        Study study = dataService.findStudy(submission.getStudy(), submitter, wsPwdHash);
        MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, submitter, wsPwdHash, submission);
        bySubmission.forEach(subject -> subject.setStudyProtocolName(metadata.getProtocolName())); //TODO: Refactor setting studyOID out of validation , this is not the right place to do it
        subjectRepository.save(bySubmission);
        List<StudySubjectWithEventsType> subjectWithEventsTypes = openClinicaService
                .getStudySubjectsType(submitter.getUsername(), wsPwdHash, submitter.getOcEnvironment(), study.getIdentifier(), "");

        List<ValidationErrorMessage> errors = new ArrayList<>();
        PatientDataOcChecks checksRunner = new PatientDataOcChecks(metadata, bySubmission, subjectWithEventsTypes, subjectsInData);
        errors.addAll(checksRunner.getErrors());
        subjectRepository.save(bySubmission);
        return errors;
    }

    /**
     * Sets study field in UploadSubmission, inferring from the data submitted by the user.
     * Please mind that UploadSession is not saved - this method is called
     * on every validation run - to account for possible changes in user data (if for instance, resubmitting was
     * possible)
     * @param entries
     * @param submission
     */
    private void determineStudy(Collection<ClinicalData> entries, UploadSession submission) {
        Set<String> usedStudyOIDs = entries.stream().map(ocEntity -> ocEntity.getStudy()).collect(Collectors.toSet());
        if (usedStudyOIDs.size() > 1) log.error("Attempted validation of file referencing multiple studies");
        submission.setStudy(usedStudyOIDs.stream().findFirst().get()); // Multiple studies not allowed, checked by a validator
    }

    /**
     * Responsible for finding errors in the data that would prevent displaying mapping view.
     * This method does not check however for data format errors - those are checked by FileValidator.
     *
     * @param submission
     * @param wsPwdHash
     * @return
     * @throws Exception
     */
    public Collection<ValidationErrorMessage> dataPremappingValidation(UploadSession submission, String wsPwdHash, MetaDataProvider metaDataProvider) throws Exception {
        List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(submission);
        determineStudy(bySubmission, submission);
        OcUser submitter = submission.getOwner();
        Study study = dataService.findStudy(submission.getStudy(), submitter, wsPwdHash);
        MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, submitter, wsPwdHash, submission);
        List<StudySubjectWithEventsType> subjectWithEventsTypes = openClinicaService
                .getStudySubjectsType(submitter.getUsername(), wsPwdHash, submitter.getOcEnvironment(), study.getIdentifier(), "");
        List<ValidationErrorMessage> errors = new ArrayList<>();

        ClinicalDataChecksRunner checksRunner = new DataPreMappingValidator(metadata, bySubmission, subjectWithEventsTypes);
        errors.addAll(checksRunner.getErrors());
        return errors;
    }
}
