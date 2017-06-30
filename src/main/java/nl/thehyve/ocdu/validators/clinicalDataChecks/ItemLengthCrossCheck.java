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
import nl.thehyve.ocdu.models.errors.FieldLengthExceeded;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 11/05/16.
 */
public class ItemLengthCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<ClinicalData, Integer> lengthMap = buildFieldLengthMap(data, itemDefMap);
        FieldLengthExceeded error = new FieldLengthExceeded();
        data.stream().forEach(clinicalData -> {
            String value = clinicalData.getValue();
            if (lengthMap.get(clinicalData) != null && value.length() > lengthMap.get(clinicalData)) {
                if (lengthMap.get(clinicalData) != 0) { // Length does not have to be defined, in this case it is 0
                    error.addOffendingValue(clinicalData.toOffenderString() + " Exceeds allowed length: "
                            + lengthMap.get(clinicalData));
                }
            }
        });
        if (error.getOffendingValues().size() > 0) {
            return error;
        } else return null;
    }

    Map<ClinicalData, Integer> buildFieldLengthMap(List<ClinicalData> data, Map<ClinicalData, ItemDefinition> itemDefMap) {
        Map<ClinicalData, Integer> lengthMap = new HashMap<>();
        data.forEach(clinicalData -> {
            ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
            Integer length = 0;
            if (itemDefinition != null) length = itemDefinition.getLength(); // zero means no check
            lengthMap.put(clinicalData, length);
        });
        return lengthMap;
    }

}
