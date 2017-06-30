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
import nl.thehyve.ocdu.models.OcDefinitions.RangeCheck;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.RangeCheckViolation;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by piotrzakrzewski on 15/05/16.
 */
public class RangeChecks implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        RangeCheckViolation error = new RangeCheckViolation();
        Set<String> alreadyReported = new HashSet<>();
        data.forEach(clinicalData -> {
            ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
            if (itemDefinition != null) { // Nonexistent item is a separate error
                List<RangeCheck> rangeCheckList = itemDefinition.getRangeCheckList();
                rangeCheckList.forEach(rangeCheck -> {
                    List<String> values = clinicalData.getValues(itemDefMap.get(clinicalData).isMultiselect());
                    for (String value : values) {
                        if (UtilChecks.isFloat(value) || UtilChecks.isInteger(value)) {
                            BigDecimal intValue = BigDecimal.valueOf(Double.parseDouble(value)); // Do not attempt floating point comparison
                            BigDecimal rangeValue = rangeCheck.getValue();
                            String dataType = itemDefinition.getDataType();
                            if ("integer".equals(dataType)) {
                                rangeValue = rangeValue.setScale(0);
                                intValue = intValue.setScale(0);
                            }

                            if (!rangeCheck.isInRange(intValue)) {
                                String msg = clinicalData.toOffenderString()+ " " + rangeCheck.violationMessage() + rangeValue
                                        + " but was: " + intValue;
                                if (!alreadyReported.contains(msg)) {
                                    error.addOffendingValue(msg);
                                    clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
                                    alreadyReported.add(msg);
                                }

                            }
                        } // If item is not numeric but should be - there is a separate error for that
                    }
                });
            }
        });
        if (error.getOffendingValues().size() > 0) {
            return error;
        } else
            return null;
    }
}
