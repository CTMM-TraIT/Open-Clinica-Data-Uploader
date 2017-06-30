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
import nl.thehyve.ocdu.models.errors.StudyStatusError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class StudyStatusAvailable implements ClinicalDataCrossCheck {

    public static final String STUDY_STATUS_ALLOWING_UPLOAD = "available";

    /**
     * Map between the technical study status returned by the Metadata web-service call and the value is is displayed to
     * the users.
     */
    private static final Map<String, String> OPENCLINCA_STATUS_MAP = new HashMap<>();
    static {
        OPENCLINCA_STATUS_MAP.put("pending", "Design");
        OPENCLINCA_STATUS_MAP.put(STUDY_STATUS_ALLOWING_UPLOAD, "Available");
        OPENCLINCA_STATUS_MAP.put("frozen", "Frozen");
        OPENCLINCA_STATUS_MAP.put("locked", "Locked");
    }

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        if (! STUDY_STATUS_ALLOWING_UPLOAD.equals(metaData.getStatus())) {
            ValidationErrorMessage error = new StudyStatusError();
            error.addOffendingValue("Study: " + metaData.getStudyName() + " has status: " +  OPENCLINCA_STATUS_MAP.get(metaData.getStatus()));
            UtilChecks.addErrorClassificationToAll(data, ErrorClassification.BLOCK_ENTIRE_UPLOAD);
            return error;
        }
        return null;
    }
}
