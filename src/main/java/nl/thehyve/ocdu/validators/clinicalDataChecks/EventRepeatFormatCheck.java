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
import nl.thehyve.ocdu.models.errors.EventRepeatFormatError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the event repeat is a valid integer.
 * Created by jacob on 8/3/16.
 */
public class EventRepeatFormatCheck implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        EventRepeatFormatError error = new EventRepeatFormatError();
        Map<String, Set<String>> repeatMap = data.stream()
                .collect(Collectors.groupingBy(ClinicalData::getSsid,
                        Collectors.mapping(ClinicalData::getEventRepeat, Collectors.toSet())));

        Set<String> subjectIDSetWithError = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : repeatMap.entrySet()) {
            Set<String> repeatsPerSubject = entry.getValue();
            for (String repeatValue : repeatsPerSubject) {
                try {
                    Integer.parseInt(repeatValue);
                } catch (NumberFormatException nfe) {
                    error.addOffendingValue(entry.getKey());
                    subjectIDSetWithError.add(entry.getKey());
                }
            }
        }
        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, subjectIDSetWithError, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        }
        return null;
    }
}
