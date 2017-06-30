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
import nl.thehyve.ocdu.models.errors.DataTypeMismatch;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;

/**
 * Created by piotrzakrzewski on 12/05/16.
 */
public class DataTypeCrossCheck implements ClinicalDataCrossCheck {


    private static Map<String, String> humanReadbleTypes = initHumanReadbleTypes();

    private static Map<String, String> initHumanReadbleTypes() {
        Map<String, String> humanReadble = new HashMap<>();
        humanReadble.put(UtilChecks.TEXT_DATA_TYPE, "text");
        humanReadble.put(UtilChecks.INTEGER_DATA_TYPE, "integer number (e.g. 2)");
        humanReadble.put(UtilChecks.FLOAT_DATA_TYPE, "real number (e.g. 12.3)");
        humanReadble.put(UtilChecks.PARTIAL_DATE_DATA_TYPE, "partial date (e.g: 1996)");
        humanReadble.put(UtilChecks.DATE_DATA_TYPE, "Date format is invalid or date does not exist. The date format " +
                "should be dd-mm-yyyy. For example, 23-10-2012");
        return humanReadble;
    }

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<ClinicalData, String> itemDataTypes = buildDataTypeMap(data, itemDefMap);
        Set<String> offenders = new HashSet<>();
        for (ClinicalData clinicalData : data) {
            ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
            String expectedType = itemDataTypes.get(clinicalData);
            if ((itemDefinition != null) && (StringUtils.isNotBlank(expectedType))) {
                List<String> values = clinicalData.getValues(itemDefinition.isMultiselect());
                if (! UtilChecks.allValuesMatch(values, expectedType)) {
                    if (StringUtils.isNotEmpty(clinicalData.getValue()) && (itemDefinition.getCodeListRef() == null)) {
                        offenders.add(clinicalData.toOffenderString() + " Expected: " + humanReadbleTypes.get(itemDataTypes.get(clinicalData)));
                        clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
                    }
                }
            }
        }

/*        Set<String> offenders = data.stream()
                .filter(clinicalData -> !allValuesMatch(clinicalData.getValues(itemDefMap.get(clinicalData).isMultiselect()), itemDataTypes.get(clinicalData)) && shownMap.get(clinicalData))
                .map(clinicalData -> clinicalData.toOffenderString() + " Expected: " + humanReadbleTypes.get(itemDataTypes.get(clinicalData)))
                .collect(Collectors.toSet());*/
        if (offenders.size() > 0) {
            DataTypeMismatch error = new DataTypeMismatch();
            offenders.stream().
                    forEach(offender -> {
                        error.addOffendingValue(offender);
                    });
            return error;
        } else return null;
    }

    private Map<ClinicalData, String> buildDataTypeMap(List<ClinicalData> data, Map<ClinicalData, ItemDefinition> defMap) {
        Map<ClinicalData, String> typeMap = new HashMap<>();
        for (ClinicalData clinicalData : data) {
            ItemDefinition def = defMap.get(clinicalData);
            if (def != null) {
                typeMap.put(clinicalData, def.getDataType());
            }
        }
        return typeMap;
    }
}
