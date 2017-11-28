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

import nl.thehyve.ocdu.models.MetaDataTree;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

/**
 * Study Metadata related calls used on the frontend.
 *
 * Created by piotrzakrzewski on 07/05/16.
 */
@RestController
@RequestMapping("/metadata")
public class MetadataController {

    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    DataService dataService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    OcUserService ocUserService;


    /**
     * Returns tree made of study metadata. Currently not used on the frontend - can be used again should we
     * return to mapping more than one CRF-version contents at a time.
     * @param session
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public ResponseEntity<?> getMetadata(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwd = ocUserService.getOcwsHash(session);
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            MetaDataTree tree = dataService.getMetadataTree(currentUploadSession, pwd, metaDataProvider);
            return new ResponseEntity<>(tree, HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            String errorMessage = "no submission active";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns OC paths (study/event/crf/version/item) for the CRF-version targeted by the user.
     * @param session
     * @return
     */
    @RequestMapping(value = "/targetedCrf", method = RequestMethod.GET)
    public ResponseEntity<?> targetedCrf(HttpSession session) {
        try {
            UploadSession currentUploadSession = uploadSessionService.getCurrentUploadSession(session);
            String pwd = ocUserService.getOcwsHash(session);
            MetaDataProvider metaDataProvider = new HttpSessionMetaDataProvider(session);
            Collection<String> targetPaths = dataService.getTargetCrf(currentUploadSession, pwd, metaDataProvider);
            Collections.sort((List<String>) targetPaths);
            return new ResponseEntity<>(targetPaths, HttpStatus.OK);
        } catch (UploadSessionNotFoundException e) {
            e.printStackTrace();
            String errorMessage = "no submission active";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
