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

package nl.thehyve.ocdu.controllers;

import nl.thehyve.ocdu.factories.EventDataFactory;
import nl.thehyve.ocdu.factories.PatientDataFactory;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import nl.thehyve.ocdu.services.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides file templates for the user.
 * Created by bo on 6/17/16.
 */
@Controller
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    OcUserService ocUserService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    DataService dataService;

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    @Autowired
    MetaDataService metaDataService;


    @RequestMapping(value = "/get-subject-template", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getSubjectTemplate(@RequestParam("registerSite") boolean registerSite, HttpSession session) {
        try {
            UploadSession uploadSession = uploadSessionService.getCurrentUploadSession(session);
            OcUser user = ocUserService.getCurrentOcUser(session);
            String username = user.getUsername();
            String pwdHash = ocUserService.getOcwsHash(session);
            String url = user.getOcEnvironment();
            List<ClinicalData> clinicalDataList = clinicalDataRepository.findBySubmission(uploadSession);

            //key: subject id from user - val: technical subject id
            Map<String, String> subjectMap = openClinicaService.createMapSubjectLabelToSubjectOID(username, pwdHash, url, clinicalDataList);

            Study study = dataService.findStudy(uploadSession.getStudy(), user, pwdHash);

            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, user, pwdHash, uploadSession);
            boolean onlyYearOfBirthUsed = metadata.getBirthdateRequired() == MetaData.BIRTH_DATE_AS_ONLY_YEAR;
            PatientDataFactory pdf = new PatientDataFactory(user, uploadSession, onlyYearOfBirthUsed);

            Map<String, String> subjectSiteMap = new HashMap<>();
            for (ClinicalData clinicalData : clinicalDataList) {
                if (! subjectSiteMap.containsKey(clinicalData.getSsid())) {
                    subjectSiteMap.put(clinicalData.getSsid(), clinicalData.getSite());
                }
            }

            List<String> result = pdf.generatePatientRegistrationTemplate(metadata, subjectMap, registerSite, subjectSiteMap);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/get-event-template", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getEventTemplate(HttpSession session) {
        try {
            UploadSession uploadSession = uploadSessionService.getCurrentUploadSession(session);
            OcUser user = ocUserService.getCurrentOcUser(session);
            String username = user.getUsername();
            String pwdHash = ocUserService.getOcwsHash(session);
            String url = user.getOcEnvironment();
            Study study = dataService.findStudy(uploadSession.getStudy(), user, pwdHash);
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            MetaData metadata = metaDataService.retrieveMetaData(metaDataProvider, user, pwdHash, uploadSession);

            EventDataFactory edf = new EventDataFactory(user, uploadSession);
            Set<ImmutablePair> patientsInEvent = dataService.getPatientsInEvent(uploadSession);
            List<StudySubjectWithEventsType> subjectWithEventsTypes = openClinicaService
                    .getStudySubjectsType(username, pwdHash, url, study.getIdentifier(), "");

            List<String> result = edf.generateEventSchedulingTemplate(metadata, subjectWithEventsTypes, patientsInEvent);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
