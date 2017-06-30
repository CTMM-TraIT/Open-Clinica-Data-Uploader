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

import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcItemMapping;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.services.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all file uploads performed by the user to the OCDU.
 *
 * Created by piotrzakrzewski on 11/04/16.
 */
@Controller
@RequestMapping("/upload")
public class UploadController {


    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    FileService fileService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    MappingService mappingService;

    @Autowired
    ValidationService validationService;

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    DataService dataService;

    @Autowired
    MetaDataService metaDataService;

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Collection<ValidationErrorMessage>> uploadFile(
            @RequestParam("uploadfile") MultipartFile uploadfile, HttpSession session) {

        try {
            OcUser user = ocUserService.getCurrentOcUser(session);
            Path locallySavedDataFile = saveFile(uploadfile);
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwd = ocUserService.getOcwsHash(session);
            Collection<ValidationErrorMessage> fileFormatErrors = fileService.depositDataFile(locallySavedDataFile, user, currentUploadSession, pwd);
            Collection<ValidationErrorMessage> allErrors = new ArrayList<>();
            if (fileFormatErrors.size() == 0) {
                MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
                // Discard any metadata previously stored on the HttpSession
                metaDataProvider.discardMetaData();
                Collection<ValidationErrorMessage> mappingPreventingErrors =
                        validationService.dataPremappingValidation(currentUploadSession, pwd, metaDataProvider);
                allErrors.addAll(mappingPreventingErrors);
            }
            allErrors.addAll(fileFormatErrors);
            return new ResponseEntity<>(allErrors, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value = "/events", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Collection<ValidationErrorMessage>> uploadEventsDataFile(
            @RequestParam("uploadEventFile") MultipartFile uploadfile, HttpSession session) {

        try {
            OcUser user = ocUserService.getCurrentOcUser(session);
            Path locallySavedDataFile = saveFile(uploadfile);
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            Collection<ValidationErrorMessage> fileFormatErrors = fileService
                    .depositEventsDataFile(locallySavedDataFile, user, currentUploadSession);
            return new ResponseEntity<>(fileFormatErrors, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    private Path saveFile(MultipartFile file) throws IOException {
        // Get the filename and build the local file path
        String filename = file.getOriginalFilename();
        String directory = System.getProperty("java.io.tmpdir");
        String filepath = Paths.get(directory, filename).toString();

        // Save the file locally
        try (BufferedOutputStream stream =
                     new BufferedOutputStream(new FileOutputStream(new File(filepath)));
             BOMInputStream bis = new BOMInputStream(file.getInputStream(), false)) {
            IOUtils.copy(bis, stream);
        }
        return Paths.get(filepath);
    }


    @RequestMapping(value = "/mapping", method = RequestMethod.POST)
    public ResponseEntity<List<OcItemMapping>> acceptMapping(HttpSession session, @RequestBody List<OcItemMapping> mappings) {
        if (!isValid(mappings)) {
            log.error("Incorrect mapping JSON provided.");
            return new ResponseEntity<>(mappings, HttpStatus.BAD_REQUEST);
        }
        try {
            UploadSession submission = uploadSessionService.getCurrentUploadSession(session);
            mappingService.applyMapping(mappings, submission);
            return new ResponseEntity<>(mappings, HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValid(List<OcItemMapping> mappings) {
        List<OcItemMapping> faulty = mappings.stream().filter(ocItemMapping -> {
            if (ocItemMapping.getCrfName() == null ||
                    ocItemMapping.getStudy() == null ||
                    ocItemMapping.getCrfVersion() == null ||
                    ocItemMapping.getEventName() == null ||
                    ocItemMapping.getOcItemName() == null ||
                    ocItemMapping.getUsrItemName() == null) return true;
            else return false;
        }).collect(Collectors.toList());
        if (faulty.size() > 0) {
            return false;
        } else return true;
    }

    @RequestMapping(value = "/subjects", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Collection<ValidationErrorMessage>> uploadPatientFile(
            @RequestParam("uploadPatientData") MultipartFile uploadPatientData, HttpSession session) {

        try {
            OcUser user = ocUserService.getCurrentOcUser(session);
            Path locallySavedDataFile = saveFile(uploadPatientData);
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);

            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            String pwdHash = ocUserService.getOcwsHash(session);
            MetaData metaData = metaDataService.retrieveMetaData(metaDataProvider, user, pwdHash, currentUploadSession);

            boolean onlyYearOfBirthUsed = (metaData.getBirthdateRequired() == 2);
            Collection<ValidationErrorMessage> fileFormatErrors = fileService.depositPatientFile(locallySavedDataFile, user, currentUploadSession, onlyYearOfBirthUsed);
            return new ResponseEntity<>(fileFormatErrors, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
}
