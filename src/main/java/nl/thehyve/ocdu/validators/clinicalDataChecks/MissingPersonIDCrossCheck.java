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
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.MissingPersonIDError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the person ID is present in the subject data if this field is defined as required in the study. Only active
 * for new subjects, existing subjects can identified using only the <code>StudySubjectID (ssid)</code>.
 * Created by jacob on 8/3/16.
 */
public class MissingPersonIDCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        MissingPersonIDError error = new MissingPersonIDError();

        List<String> existingStudySubjectIDList =
                studySubjectWithEventsTypeList.stream()
                        .map(StudySubjectWithEventsType::getLabel)
                        .collect(Collectors.toList());

        if (metaData.getPersonIDUsage() == ProtocolFieldRequirementSetting.MANDATORY) {
            List<ClinicalData> newSubjectInStudy = data.stream()
                                                    .filter(clinicalData -> ! existingStudySubjectIDList.contains(clinicalData.getSsid()))
                                                    .collect(Collectors.toList());
            Set<String> violatorSet = newSubjectInStudy.stream()
                                        .filter(clinicalData -> StringUtils.isEmpty(clinicalData.getPersonID()))
                                        .map(ClinicalData::getSsid)
                                        .collect(Collectors.toSet());

            UtilChecks.addErrorClassificationForSubjects(data, violatorSet, ErrorClassification.BLOCK_ENTIRE_CRF);
            error.addAllOffendingValues(violatorSet);
        }
        if (error.getOffendingValues().size() > 0)
            return error;
        else return null;
    }
}
