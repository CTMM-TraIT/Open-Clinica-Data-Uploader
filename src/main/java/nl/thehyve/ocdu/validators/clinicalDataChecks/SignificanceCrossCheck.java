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
import nl.thehyve.ocdu.models.errors.TooManySignificantDigits;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 16/05/16.
 */
public class SignificanceCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        TooManySignificantDigits error = new TooManySignificantDigits();
        data.forEach(clinicalData -> {
            ItemDefinition definition = itemDefMap.get(clinicalData);
            if (definition != null) {
                addOffendingValues(error, clinicalData, definition);
            }
        });
        if (error.getOffendingValues().size() > 0) {
            return error;
        } else
            return null;
    }

    private void addOffendingValues(TooManySignificantDigits error, ClinicalData clinicalData, ItemDefinition definition) {
        for (String value : clinicalData.getValues(definition.isMultiselect())) {
            int digitsAfterDM = getDigitsAfterDM(value);
            if (digitsAfterDM > definition.getSignificantDigits()) {
                clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
                error.addOffendingValue(clinicalData.toOffenderString() + " expected number of significant digits: "
                        + definition.getSignificantDigits());
            }
        }
    }

    private int getDigitsAfterDM(String value) {
        if (!UtilChecks.isFloat(value)) {
            return 0;
        } else {
            String[] split = value.split("\\.");
            if (split.length != 2) {
                return 0;
            } else {
                String afterDM = split[1];
                return afterDM.length();
            }
        }
    }
}
