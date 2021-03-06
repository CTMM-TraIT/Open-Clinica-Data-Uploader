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

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import nl.thehyve.ocdu.repositories.UploadSessionRepository;
import nl.thehyve.ocdu.services.*;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

/**
 * Calls related to the state of user submission (UploadSession).
 * Created by bo on 4/26/16.
 */
@RestController
@RequestMapping("/submission")
public class UploadSessionController {


    private static final String ALPHABETH_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABETH_LOWERCASE = ALPHABETH_UPPERCASE.toLowerCase();
    private static final String NUMBERS = "0123456789";
    private static final String EXTRA_CHARACTERS = " _-";
    private static final Logger log = LoggerFactory.getLogger(UploadSessionController.class);

    public static final String ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME = ALPHABETH_UPPERCASE + ALPHABETH_LOWERCASE + NUMBERS + EXTRA_CHARACTERS;

    @Autowired
    DataService dataService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    UploadSessionRepository uploadSessionRepository;

    @Autowired
    UploadSessionService uploadSessionService;

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    public String username() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public ResponseEntity<DataService.FieldsDetermined> getFieldsInfo(HttpSession session) {
        try {
            UploadSession submission = uploadSessionService.getCurrentUploadSession(session);
            DataService.FieldsDetermined info = dataService.getInfo(submission);
            if (info == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(info, OK);
            }
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<UploadSession> updateSubmission(@RequestParam(value = "step") String step, HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            UploadSession.Step _step;
            switch (step) {
                case "mapping":
                    _step = UploadSession.Step.MAPPING;
                    break;
                case "feedback-data":
                    _step = UploadSession.Step.FEEDBACK_DATA;
                    break;
                case "subjects":
                    _step = UploadSession.Step.SUBJECTS;
                    break;
                case "feedback-subjects":
                    _step = UploadSession.Step.FEEDBACK_SUBJECTS;
                    break;
                case "events":
                    _step = UploadSession.Step.EVENTS;
                    break;
                case "feedback-events":
                    _step = UploadSession.Step.FEEDBACK_EVENTS;
                    break;
                case "pre-odm-upload":
                    _step = UploadSession.Step.PRE_ODM_UPLOAD;
                    break;
                case "odm-upload":
                    _step = UploadSession.Step.ODM_UPLOAD;
                    break;
                case "final":
                    _step = UploadSession.Step.FINAL;
                    break;
                default:
                    _step = UploadSession.Step.MAPPING;
                    break;
            }
            currentUploadSession.setStep(_step);
            uploadSessionRepository.save(currentUploadSession);
            return new ResponseEntity<>(OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseEntity<?> deleteSubmission(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(currentUploadSession);
            clinicalDataRepository.delete(bySubmission);
            uploadSessionRepository.delete(currentUploadSession);
            uploadSessionService.setCurrentUploadSession(session, null);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/deleteSession", method = RequestMethod.POST)
    public ResponseEntity<?> deleteSubmissionById(@RequestParam(value = "id") long longsessionid, HttpSession session) {
        try {
            UploadSession selectedSession = uploadSessionRepository.findOne(longsessionid);
            if (selectedSession != null) {
                OcUser currentOcUser = ocUserService.getCurrentOcUser(session);
                long userId1 = currentOcUser.getId();
                long userId2 = selectedSession.getOwner().getId();
                if (userId1 == userId2) {
                    List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(selectedSession);
                    clinicalDataRepository.delete(bySubmission);
                    uploadSessionRepository.delete(selectedSession);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<List<UploadSession>> unfinishedSessions(HttpSession session) {
        try {
            OcUser ocUser = ocUserService.getCurrentOcUser(session);
            List uploadSessions = uploadSessionRepository.findByOwner(ocUser);

            return new ResponseEntity<>(uploadSessions, OK);
        } catch (UploadSessionNotFoundException ex) {
            System.out.println(ex);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST) //TODO: add tests
    public ResponseEntity<?> createUploadSession(@RequestParam(value = "name", defaultValue = "newSession") String name,
                                                 HttpSession session) {
        try {
            String sanitizedName = UtilChecks.inputValidation(name, ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, "submission name", 50);
            OcUser usr = ocUserService.getCurrentOcUser(session);
            UploadSession uploadSession = new UploadSession(sanitizedName, UploadSession.Step.MAPPING, new Date(), usr);//TODO: Add remaining steps
            uploadSessionRepository.save(uploadSession);
            uploadSessionService.setCurrentUploadSession(session, uploadSession);
            return new ResponseEntity<>(uploadSession, OK);
        } catch (UploadSessionNotFoundException ex) {
            System.out.println(ex);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (InputValidationException ive) {
            log.info("Input validation problem: " + name);
            return new ResponseEntity<>(ive.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ResponseEntity<UploadSession> currentSession(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            return new ResponseEntity<>(currentUploadSession, OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public ResponseEntity<?> selectSession(@RequestParam(value = "sessionId") Long sessionId, HttpSession session) {
        try {
            UploadSession requested = uploadSessionRepository.findOne(sessionId);
            UploadSession.Step previousStep = requested.getStep();
            if ((previousStep == UploadSession.Step.ODM_UPLOAD) ||
                    (previousStep == UploadSession.Step.FINAL) ||
                    (previousStep == UploadSession.Step.PRE_ODM_UPLOAD)) {
                requested.setStep(UploadSession.Step.ODM_UPLOAD);
            }
            if (requested.getOwner().getId() == ocUserService.getCurrentOcUser(session).getId()) {
                uploadSessionService.setCurrentUploadSession(session, requested);
                return new ResponseEntity<>(OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (UploadSessionNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/user-items")
    public ResponseEntity<List<String>> getUserItems(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            List<String> userItems = dataService.getUserItems(currentUploadSession);
            Collections.sort(userItems);
            return new ResponseEntity<>(userItems, OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @Autowired
    MappingService mappingService;

    @RequestMapping(value = "/current-mapping")
    public ResponseEntity<Map<String, String>> getCurrentMapping(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            Map<String, String> mapping = mappingService.getCurrentMapping(currentUploadSession);
            return new ResponseEntity<>(mapping, OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
}
