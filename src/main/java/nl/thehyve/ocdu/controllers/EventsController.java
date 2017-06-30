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

import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.repositories.EventRepository;
import nl.thehyve.ocdu.repositories.SubjectRepository;
import nl.thehyve.ocdu.services.*;
import nl.thehyve.ocdu.validators.EventDataOcChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventsController {

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    DataService dataService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    SubjectRepository subjectRepository;


    @RequestMapping(value = "/register-event", method = RequestMethod.POST)
    public ResponseEntity<String> registerEvents(HttpSession session) {
        try {
            UploadSession uploadSession = uploadSessionService.getCurrentUploadSession(session);
            OcUser user = ocUserService.getCurrentOcUser(session);
            String username = user.getUsername();
            String pwdHash = ocUserService.getOcwsHash(session);
            String url = user.getOcEnvironment();

            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            MetaData metaData = dataService.getMetaData(uploadSession, pwdHash, metaDataProvider);
            Study study = dataService.findStudy(uploadSession.getStudy(), user, username);

            List<StudySubjectWithEventsType> studySubjectWithEventsTypeList =
                    openClinicaService.getStudySubjectsType(username, pwdHash, url, study.getIdentifier(), "");

            List<Event> eventList = eventRepository.findBySubmission(uploadSession);
            List<Subject> subjectList = subjectRepository.findBySubmission(uploadSession);
            EventDataOcChecks eventDataOcChecks =
                    new EventDataOcChecks(metaData, eventList, subjectList, studySubjectWithEventsTypeList);
            List<ValidationErrorMessage> validationErrorMessageList = eventDataOcChecks.getErrors();
            if (! validationErrorMessageList.isEmpty()) {
                return new ResponseEntity(validationErrorMessageList, HttpStatus.BAD_REQUEST);
            }

            List<StudySubjectWithEventsType> subjectWithEventsTypes = openClinicaService
                    .getStudySubjectsType(username, pwdHash, url, study.getIdentifier(), "");

            openClinicaService.scheduleEvents(username, pwdHash, url, metaData, eventList, subjectWithEventsTypes);
            return new ResponseEntity<>("", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
    }
}
