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
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All ClinicalData validation logic which relies on OpenClinica metadata should be implemented in classes
 * implementing this interface. Each validation rule should have a separate class.
 * These checks are meant to be run by ClinicalDataChecksRunner, which makes sure are inputs are provided as
 * necessary.
 *
 * Created by piotrzakrzewski on 04/05/16.
 */
public interface ClinicalDataCrossCheck {

    ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData,
                                                 Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType>                                                         studySubjectWithEventsTypeList, Map<ClinicalData,
                                                        Boolean> shownMap,
                                                 Map<String, Set<CRFDefinition>> eventMap);
    // All the maps are present here for performance reasons - precomputing all the maps before running checks is much
    // more efficient.

}
