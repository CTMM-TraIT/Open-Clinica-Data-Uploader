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
import nl.thehyve.ocdu.models.OcItemMapping;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All methods related to mapping clinical data subitted by the user with names defined
 * in open clinica study metadata.
 *
 * Created by piotrzakrzewski on 07/05/16.
 */
@Service
public class MappingService {

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    private static final Logger log = LoggerFactory.getLogger(MappingService.class);

    /**
     * Change item names in user submission (UploadSession) according to the mapping.
     * Changes are saved to the database.
     *
     * @param mappings
     * @param submission
     */
    public void applyMapping(List<OcItemMapping> mappings, UploadSession submission) {
        List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(submission);
        List<OcItemMapping> nonTrivial = mappings.stream().filter(ocItemMapping ->
                !ocItemMapping.getUsrItemName().equals(ocItemMapping.getOcItemName())).collect(Collectors.toList());
        log.debug("Provided: " + nonTrivial.size() + " non trival mappings.");
        nonTrivial.stream().forEach(ocItemMapping -> matchAndMap(ocItemMapping, bySubmission));
    }

    private void matchAndMap(OcItemMapping mapping, List<ClinicalData> data) {
        data.stream().filter(clinicalData -> match(mapping, clinicalData)).
                forEach(clinicalData -> clinicalData.setItem(mapping.getOcItemName()));
        clinicalDataRepository.save(data);
    }

    private boolean match(OcItemMapping mapping, ClinicalData data) {
        if (data.getEventName().equals(mapping.getEventName()) &&
                data.getCrfName().equals(mapping.getCrfName()) &&
                data.getCrfVersion().equals(mapping.getCrfVersion()) &&
                data.getOriginalItem().equals(mapping.getUsrItemName())
                ) {
            log.debug("Matched: " + data + " with: " + mapping);
            return true;
        } else return false;
    }

    /**
     * Return current mapping between original item names and current item names from user submission.
     *
     * @param submission
     * @return
     */
    public Map<String, String> getCurrentMapping(UploadSession submission) {
        Map<String, String> mapping = new HashMap<>();
        List<ClinicalData> bySubmission = clinicalDataRepository.findBySubmission(submission);
        bySubmission.forEach(clinicalData -> mapping.put(clinicalData.getItem(), clinicalData.getOriginalItem()));
        return mapping;
    }
}
