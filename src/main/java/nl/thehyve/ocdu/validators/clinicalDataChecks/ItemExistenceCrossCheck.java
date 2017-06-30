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
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ItemDoesNotExist;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 11/05/16.
 */
public class ItemExistenceCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<String, Set<String>> allItemNames = metaData.obtainFormOIDItemNameMap();
        Set<String> missing = data.stream().filter(clinicalData -> {
            String item = clinicalData.getItem();
            String formOID = metaData.findFormOID(clinicalData.getCrfName(), clinicalData.getCrfVersion());
            Set<String> itemPresentInForm = allItemNames.get(formOID);
            if (itemPresentInForm == null) {
                return false;
            }
            return !(itemPresentInForm.contains(item));
        }).map(clinicalData -> clinicalData.getItem()).collect(Collectors.toSet());

        if (missing.size() > 0) {
            ItemDoesNotExist error = new ItemDoesNotExist();
            missing.forEach(itemName -> {
                if (itemName.equals("")) {
                    error.addOffendingValue(" (Empty string)");
                } else {
                    error.addOffendingValue(itemName);
                }
            });
            UtilChecks.addErrorClassificationToAll(data, ErrorClassification.BLOCK_ENTIRE_UPLOAD);
            return error;
        } else {
            return null;
        }
    }
}
