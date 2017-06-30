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
import nl.thehyve.ocdu.models.OcDefinitions.ResponseType;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.TooManyValues;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 15/05/16.
 */
public class ValuesNumberCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        List<ClinicalData> violators = data.stream()
                .filter(clinicalData -> isViolator(clinicalData, itemDefMap) && shownMap.get(clinicalData)).collect(Collectors.toList());
        if (violators.size() > 0) {
            TooManyValues error = new TooManyValues();
            violators.forEach(clinicalData -> {
                String msg = clinicalData.toOffenderString() + " Number of values (separated by comma): " +
                        clinicalData.getValues(itemDefMap.get(clinicalData).isMultiselect()).size();
                error.addOffendingValue(msg);
                clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
            });
            return error;
        } else
            return null;
    }

    private boolean isViolator(ClinicalData dataPoint, Map<ClinicalData, ItemDefinition> itemDefMap) {
        ItemDefinition itemDefinition = itemDefMap.get(dataPoint);
        if (itemDefinition == null) {
            return false; // Missing item is a different error
        }
        boolean hasMultipleValues = dataPoint.getValues(itemDefMap.get(dataPoint).isMultiselect()).size() > 1;
        if (hasMultipleValues && itemDefinition.getResponseType() == ResponseType.SINGLE_SELECT) {
            return true;
        } else {
            return false;
        }
    }
}
