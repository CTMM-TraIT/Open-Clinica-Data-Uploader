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

package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.SSIDDuplicated;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 16/05/16.
 */
public class SsidUniqueCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        HashMap<String, List<String>> rowMap = new HashMap<>();
        Map<String, String> rowKeyToSubjectIDMap = new HashMap<>();
        data.stream().forEach(clinicalData -> {
            String rowString = toRowIdString(clinicalData);
            List<String> items;
            if (!rowMap.containsKey(rowString)) {
                items = new ArrayList<>();
                rowMap.put(rowString, items);
                rowKeyToSubjectIDMap.put(rowString, clinicalData.getSsid());
            } else {
                items = rowMap.get(rowString);
            }
            items.add(clinicalData.getItem());
        });
        SSIDDuplicated error = new SSIDDuplicated();
        List<String> offenders = getOffenders(rowMap);
        error.addAllOffendingValues(offenders);
        if (error.getOffendingValues().size() > 0) {
            Set<String> subjectIDsWithError = new HashSet<>();
            for (String rowKey : offenders) {
                String subjectID = rowKeyToSubjectIDMap.get(rowKey);
                subjectIDsWithError.add(subjectID);
            }

            UtilChecks.addErrorClassificationForSubjects(data, subjectIDsWithError, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else
            return null;
    }

    private List<String> getOffenders(HashMap<String, List<String>> rowMap) {
        List<String> offenders = new ArrayList<>();
        for (String key : rowMap.keySet()) {
            List<String> items = rowMap.get(key);
            List<String> uniqueItems = items.stream().distinct().collect(Collectors.toList());
            if (items.size() != uniqueItems.size()) {
                offenders.add(key);
            }
        }
        return offenders;
    }

    private String toRowIdString(ClinicalData clinicalData) {
        String gRepeat = clinicalData.getGroupRepeat() != null ? clinicalData.getGroupRepeat().toString() : "";
        return clinicalData.getSsid() + " in: "
                + clinicalData.getEventName() + " "
                + clinicalData.getEventRepeat() + ", "
                + clinicalData.getCrfName() + " "
                + gRepeat + " "
                + clinicalData.getCrfVersion();
    }
}
