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
import nl.thehyve.ocdu.models.errors.RepeatInNonrepeatingItem;
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
public class ItemGroupRepeat implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        RepeatInNonrepeatingItem error = new RepeatInNonrepeatingItem();
        Set<String> reportedItems = new HashSet<>();
        Set<String> offenderSubjectIDs = new HashSet<>();
        data.stream().forEach(
                clinicalData -> {
                    boolean repeating = !(clinicalData.getGroupRepeat() == null);
                    // Only allowed way to get null as group repeat number is not to include anything past _ in item
                    // column. This means item is non-repeating.
                    ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
                    boolean expectedToBeRepeating = true;
                    if (itemDefinition != null) { // Missing item is a separate check
                        expectedToBeRepeating = itemDefinition.isRepeating();
                        String reportedItem = clinicalData.getItem() + clinicalData.getGroupRepeat();
                        if (repeating && !expectedToBeRepeating && !reportedItems.contains(reportedItem)) {
                            error.addOffendingValue("Item: " + clinicalData.getItem() +
                                    " does not belong to a repeating group, while its repeat literal in the submission " +
                                    "file is: " + clinicalData.getGroupRepeat());
                            reportedItems.add(reportedItem);
                            offenderSubjectIDs.add(clinicalData.getSsid());
                        } else if (!repeating && expectedToBeRepeating && !reportedItems.contains(reportedItem)) {
                            error.addOffendingValue("Item: " + clinicalData.getItem() +
                                    " belongs to a repeating group, while it does not have a repeat specified");
                            reportedItems.add(reportedItem);
                            offenderSubjectIDs.add(clinicalData.getSsid());
                        }
                    }
                }
        );
        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else
            return null;
    }
}
