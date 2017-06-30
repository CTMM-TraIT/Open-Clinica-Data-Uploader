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
import nl.thehyve.ocdu.models.errors.SubjectSiteMismatch;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

public class SiteSubjectMatchCrossCheck implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<String, String> subjectSiteCombinationsPresentInStudy = new HashMap<>();
        studySubjectWithEventsTypeList.
                stream().forEach(studySubjectWithEventsType -> subjectSiteCombinationsPresentInStudy.put(studySubjectWithEventsType.getLabel(), studySubjectWithEventsType.getStudyRef().getSiteRef() != null ? studySubjectWithEventsType.getStudyRef().getSiteRef().getIdentifier() : ""));

        List<ClinicalData> violators = data.stream()
                .filter(clinicalData -> (((! SitesExistCrossCheck.EMPTY_SITE_DENOTATION.equals(clinicalData.getSite()) &&
                                        (clinicalData.getSite() != null) &&
                                        (subjectSiteCombinationsPresentInStudy.containsKey(clinicalData.getSsid()))) &&
                                        (!(subjectSiteCombinationsPresentInStudy.get(clinicalData.getSsid()).equals(clinicalData.getSite()))))))
                .collect(Collectors.toList());
        if (violators.size() > 0) {
            ValidationErrorMessage error =
                    new SubjectSiteMismatch();
            Set<String> mismatchingSubjectSiteCombinationList = new HashSet<>();
            Set<String> offenderSubjectIDs = new HashSet<>();
            violators.stream().forEach(clinicalData -> {
                    String subjectID = "Subject " + ClinicalData.CD_SEP_PREFIX + clinicalData.getSsid() + ClinicalData.CD_SEP_POSTEFIX +
                                       "Line number " + ClinicalData.CD_SEP_PREFIX + clinicalData.getLineNumber() + ClinicalData.CD_SEP_POSTEFIX;
                    mismatchingSubjectSiteCombinationList.add(subjectID);
                    offenderSubjectIDs.add(clinicalData.getSsid());
            });
            error.addAllOffendingValues(mismatchingSubjectSiteCombinationList);
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;

    }
}
