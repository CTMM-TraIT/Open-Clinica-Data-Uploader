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
import nl.thehyve.ocdu.models.errors.SiteDoesNotExist;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

public class SitesExistCrossCheck implements ClinicalDataCrossCheck {

    public static final String EMPTY_SITE_DENOTATION = "";

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Set<String> siteUniqueIDsPresentInStudy = new HashSet<>();
        siteUniqueIDsPresentInStudy.add(EMPTY_SITE_DENOTATION);
        metaData.getSiteDefinitions().stream().forEach(siteDefinition -> siteUniqueIDsPresentInStudy.add(siteDefinition.getUniqueID()));
        List<ClinicalData> violators = data.stream()
                .filter(clinicalData -> (!siteUniqueIDsPresentInStudy.contains(clinicalData.getSite())) && (clinicalData.getSite() != null))
                .collect(Collectors.toList());
        if (violators.size() > 0) {
            ValidationErrorMessage error =
                    new SiteDoesNotExist();
            List<String> nonExistentSiteNames = new ArrayList<>();
            Set<String> offenderSubjectIDs = new HashSet<>();
            violators.stream().forEach(clinicalData ->
            { String siteName = ClinicalData.CD_SEP_PREFIX + clinicalData.getSite() + ClinicalData.CD_SEP_POSTEFIX + " in line " + clinicalData.getLineNumber() + " of subject: " + ClinicalData.CD_SEP_PREFIX + clinicalData.getSsid() + ClinicalData.CD_SEP_POSTEFIX;
                if (!nonExistentSiteNames.contains(siteName)) {
                    nonExistentSiteNames.add(siteName);
                    offenderSubjectIDs.add(clinicalData.getSsid());
                }
            });
            error.addAllOffendingValues(nonExistentSiteNames);
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;
    }
}
