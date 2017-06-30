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
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.thehyve.ocdu.validators.fileValidators.GenericFileValidator.MAX_ALLOWED_FIELD_LENGTH;


/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class DataFieldWidthCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData,
                                                        Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        ValidationErrorMessage error = new ValidationErrorMessage("One or more fields in your " +
                "data violate Open Clinica field width constraints, both item names (columns) and the values need to " +
                "be shorter than " + MAX_ALLOWED_FIELD_LENGTH);
        Set<String> violators = new HashSet<>();
        for (ClinicalData dataPoint : data) {
            if (isTooLong(dataPoint)) {
                violators.add(dataPoint.toOffenderString());
                dataPoint.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
            }
        }
        error.addAllOffendingValues(violators);
        if (error.getOffendingValues().size() > 0) {
            return error;
        } else return null;
    }

    private boolean isTooLong(ClinicalData dataPoint) {
        boolean itemLengthViolated = dataPoint.getValue().length() > MAX_ALLOWED_FIELD_LENGTH
                || dataPoint.getItem().length() > MAX_ALLOWED_FIELD_LENGTH;
        return itemLengthViolated;
    }
}
