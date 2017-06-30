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

import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.repositories.SubjectRepository;
import nl.thehyve.ocdu.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Triggering validation for all respective data types.
 * Created by piotrzakrzewski on 06/05/16.
 */
@RestController
@RequestMapping("/validate")
public class ValidationController {

    @Autowired
    ValidationService validationService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    OcUserService ocUserService;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public ResponseEntity<List<ValidationErrorMessage>> validateData(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwdHash = ocUserService.getOcwsHash(session);
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            List<ValidationErrorMessage> dataErrors = validationService.getDataErrors(currentUploadSession, pwdHash, metaDataProvider);
            return new ResponseEntity<>(dataErrors, HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    SubjectRepository subjectRepository;

    @RequestMapping(value = "/patients", method = RequestMethod.GET)
    public ResponseEntity<List<ValidationErrorMessage>> validatePatients(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwdHash = ocUserService.getOcwsHash(session);
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            List<ValidationErrorMessage> patientsErrors = validationService.getPatientsErrors(currentUploadSession, pwdHash, metaDataProvider);
            if(patientsErrors.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(patientsErrors, HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public ResponseEntity<List<ValidationErrorMessage>> validateEvents(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwdHash = ocUserService.getOcwsHash(session);
            OcUser ocUser = ocUserService.getCurrentOcUser(session);
            String url = ocUser.getOcEnvironment();
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            List<ValidationErrorMessage> eventsErrors = validationService.getEventsErrors(currentUploadSession, pwdHash, metaDataProvider, ocUser.getUsername(), url);
            return new ResponseEntity<>(eventsErrors,HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
