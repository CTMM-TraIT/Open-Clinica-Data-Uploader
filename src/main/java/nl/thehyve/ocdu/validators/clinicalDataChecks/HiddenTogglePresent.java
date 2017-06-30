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
import nl.thehyve.ocdu.models.OcDefinitions.DisplayRule;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ToggleVarForDisplayRuleAbsent;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 15/06/16.
 */
public class HiddenTogglePresent implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        ToggleVarForDisplayRuleAbsent error = new ToggleVarForDisplayRuleAbsent();
        Set<String> errors = new HashSet<>();
        Set<String> offenderSubjectIDs = new HashSet<>();
        for (ClinicalData clinicalData : itemDefMap.keySet()) {
            List<DisplayRule> displayRules = itemDefMap.get(clinicalData).getDisplayRules();
            for (DisplayRule displayRule : displayRules) {
                boolean exists = itemExists(data, displayRule.getControlItemName());
                if (!exists) {
                    error.addOffendingValue(clinicalData.toOffenderString() + " requires: " + displayRule.getControlItemName());
                    offenderSubjectIDs.add(clinicalData.getSsid());
                }
            }
        }
        error.addAllOffendingValues(errors);
        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;
    }

    private boolean itemExists(List<ClinicalData> data, String itemName) {
        return data.stream().anyMatch(clinicalData -> clinicalData.getItem().equals(itemName));
    }
}
