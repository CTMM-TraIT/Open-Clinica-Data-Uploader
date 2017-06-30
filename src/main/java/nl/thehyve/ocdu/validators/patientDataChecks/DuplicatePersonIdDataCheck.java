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

package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if a person ID is allready been used in a study, only for study's who have set this field to required.
 * Created by jacob on 8/8/16.
 */
public class DuplicatePersonIdDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput,
                                                        List<String> personIDInSubjectInput) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);
        String duplicateSubjectLabel = "";

        ValidationErrorMessage error = null;
        String personId = subject.getPersonId();

        if ((metaData.getPersonIDUsage() == ProtocolFieldRequirementSetting.MANDATORY) & (StringUtils.isNotEmpty(personId) & (StringUtils.isNotEmpty(ssid)))) {
            List<StudySubjectWithEventsType> subjectPresentWithPersonIDList =
                    subjectWithEventsTypes.stream()
                            .filter(studySubjectWithEventsType ->
                                personId.equals(studySubjectWithEventsType.getSubject().getUniqueIdentifier()) &&
                                        ! ssid.equals(studySubjectWithEventsType.getLabel())
                            )
                            .collect(Collectors.toList());
            if (! subjectPresentWithPersonIDList.isEmpty()) {
                StudySubjectWithEventsType subjectWithEventsType = subjectPresentWithPersonIDList.get(0);
                duplicateSubjectLabel = subjectWithEventsType.getLabel();
                error = new ValidationErrorMessage("Person ID present in subject data is already used in OpenClinica");
            }
        }

        if(error != null) {
            error.addOffendingValue(commonMessage + " person ID: " + subject.getPersonId() + " for subject label " + ssid + ", is already present for subject with label " + duplicateSubjectLabel);
        }

        return error;
    }

}
