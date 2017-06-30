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

import nl.thehyve.ocdu.factories.ClinicalDataFactory;
import nl.thehyve.ocdu.factories.EventDataFactory;
import nl.thehyve.ocdu.factories.PatientDataFactory;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import nl.thehyve.ocdu.repositories.EventRepository;
import nl.thehyve.ocdu.repositories.SubjectRepository;
import nl.thehyve.ocdu.validators.fileValidators.DataFileValidator;
import nl.thehyve.ocdu.validators.fileValidators.EventsFileValidator;
import nl.thehyve.ocdu.validators.fileValidators.PatientsFileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for depositing/accepting files submitted by the user.
 * This is the only class outside tests which should call factories deserializing file contents.
 * During file deposition appropriate validators are being used.
 *
 * Created by piotrzakrzewski on 11/04/16.
 */

@Service
public class FileService {

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    @Autowired
    EventRepository eventRepository;
    //TODO: Add events, subjects and clinical data to UploadSession so that they are destroyed when session is destroyed
    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    DataService dataService;

    @Autowired
    OcUserService ocUserService;

    public Collection<ValidationErrorMessage> depositDataFile(Path dataFile, OcUser user, UploadSession submission, String pwd) throws Exception {
        List<Study> studies = openClinicaService.listStudies(user.getUsername(), pwd, user.getOcEnvironment());
        DataFileValidator validator = new DataFileValidator(studies);
        validator.validateFile(dataFile);
        Collection<ValidationErrorMessage> errorMsgs = new ArrayList<>();
        if (validator.isValid()) {
            List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(submission);
            clinicalDataRepository.delete(bySubmission);
            ClinicalDataFactory factory = new ClinicalDataFactory(user, submission);
            List<ClinicalData> newEntries = factory.createClinicalData(dataFile);
            clinicalDataRepository.save(newEntries);
            return errorMsgs;
        } else {
            errorMsgs = validator.getErrorMessages();
            return errorMsgs;
        }
    }

    public Collection<ValidationErrorMessage> depositPatientFile(Path patientFile, OcUser user, UploadSession submission, boolean onlyYearOfBirthUsed) throws Exception {
        PatientsFileValidator validator = new PatientsFileValidator(onlyYearOfBirthUsed);
        validator.validateFile(patientFile);
        Collection<ValidationErrorMessage> errorMsgs = new ArrayList<>();
        if (validator.isValid()) {
            List<Subject> bySubmission = subjectRepository.findBySubmission(submission);
            subjectRepository.delete(bySubmission);
            PatientDataFactory factory = new PatientDataFactory(user, submission, onlyYearOfBirthUsed);

            List<Subject> newEntries = factory.createPatientData(patientFile);
            subjectRepository.save(newEntries);
            return errorMsgs;
        } else {
            errorMsgs = validator.getErrorMessages();
            return errorMsgs;
        }
    }

    public Collection<ValidationErrorMessage> depositEventsDataFile(Path dataFile,
                                                       OcUser user,
                                                       UploadSession submission) throws Exception {
        EventsFileValidator validator = new EventsFileValidator();
        validator.validateFile(dataFile);
        if (validator.isValid()) {
            List<Event> bySubmission = eventRepository.findBySubmission(submission);
            eventRepository.delete(bySubmission);
            EventDataFactory factory = new EventDataFactory(user, submission);
            List<Event> events = factory.createEventsData(dataFile);
            eventRepository.save(events);
            return Collections.emptyList();
        } else {
            return validator.getErrorMessages();
        }
    }


}
