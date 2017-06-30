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
import nl.thehyve.ocdu.models.errors.MandatoryItemInCrfMissing;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks if a mandatory item is missing in the input.
 * Created by piotrzakrzewski on 11/05/16.
 */
public class MandatoryInCrfCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        HashMap<String, Set<String>> mandatoryMap = getMandatoryMap(data, eventMap);
        HashMap<String, Set<String>> presentMap = getPresentMap(data);
        MandatoryItemInCrfMissing error = new MandatoryItemInCrfMissing();
        HashMap<String, Set<String>> remainingMandatory = reportMissingColumns(mandatoryMap, presentMap, error);
        reportMissingValues(remainingMandatory , data, error, shownMap);
        if (error.getOffendingValues().size() > 0)
            return error;
        else return null;
    }

    private void reportMissingValues(HashMap<String, Set<String>> mandatoryMap, List<ClinicalData> data, MandatoryItemInCrfMissing error, Map<ClinicalData, Boolean> shownMap) {
        Set<String> subjectIDSetWithError = new HashSet<>();
        data.stream().forEach(clinicalData -> {
            String item = clinicalData.getItem();
            String crfId = clinicalData.getCrfName() + clinicalData.getCrfVersion();
            Set<String> mandatory = mandatoryMap.get(crfId);
            String value = clinicalData.getValue();
            if (mandatory != null && mandatory.contains(item) && value.equals("") && shownMap.get(clinicalData)) { // is mandatory and value is empty, item not hidden for given subject
                error.addOffendingValue(clinicalData.toOffenderString() + " Item cannot be empty as it is mandatory in CRF.");
                subjectIDSetWithError.add(clinicalData.getSsid());
            }
        });
        Map<String, Set<String>> userItems = new HashMap<>();
        Map<String, Set<String>> mandatoryForSubject = new HashMap<>();

        data.stream().forEach(clinicalData -> {
            String crfId = clinicalData.getCrfName() + clinicalData.getCrfVersion();
            Set<String> mandatoryItems = mandatoryMap.get(crfId);
            if (!userItems.containsKey(clinicalData.getSsid())) {
                boolean shown = shownMap.get(clinicalData);
                if (shown) {
                    userItems.put(clinicalData.getSsid(), new HashSet<>());
                    mandatoryForSubject.put(clinicalData.getSsid(), mandatoryItems);
                }
            }
            Set<String> items = userItems.get(clinicalData.getSsid());
            if (items != null) {
                items.add(clinicalData.getItem());
            }
        });
        userItems.keySet().forEach(subject -> {
            Set<String> itemsUploadedForUser = userItems.get(subject);
            Set<String> mandatory = mandatoryForSubject.get(subject);
            if (mandatory != null) {
                for (String mandatoryItem : mandatory) {
                    if (!itemsUploadedForUser.contains(mandatoryItem)) {
                        error.addOffendingValue("Subject: " + subject + " misses mandatory item: " + mandatoryItem);
                        subjectIDSetWithError.add(subject);
                    }
                }
            }
        });
        UtilChecks.addErrorClassificationForSubjects(data, subjectIDSetWithError, ErrorClassification.BLOCK_ENTIRE_CRF);
    }


    private HashMap<String, Set<String>> reportMissingColumns(HashMap<String, Set<String>> mandatoryMap, HashMap<String, Set<String>> presentMap, ValidationErrorMessage error) {
        HashMap<String, Set<String>> remainingMandatory = new HashMap<>();
        for (String crfId : mandatoryMap.keySet()) {
            Set<String> expected = mandatoryMap.get(crfId);
            Set<String> found = presentMap.get(crfId);
            Set<String> remaining = new HashSet<>(expected);
            expected.stream().filter(expectedItem -> !found.contains(expectedItem)).forEach(missing -> {
                error.addOffendingValue("CRF: " + crfId + " requires item: " + missing);
                remaining.remove(missing);
            });
            remainingMandatory.put(crfId, remaining);
        }
        return remainingMandatory;
    }

    private HashMap<String, Set<String>> getPresentMap(List<ClinicalData> data) {
        HashMap<String, Set<String>> presentMap = new HashMap<>();
        data.stream().forEach(clinicalData -> {
            String crfId = clinicalData.getCrfName() + clinicalData.getCrfVersion();
            if (!presentMap.containsKey(crfId)) {
                Set<String> presentItems = new HashSet<>();
                presentItems.add(clinicalData.getItem());
                presentMap.put(crfId, presentItems);
            } else {
                Set<String> presentItems = presentMap.get(crfId);
                presentItems.add(clinicalData.getItem());
            }
        });
        return presentMap;
    }

    private HashMap<String, Set<String>> getMandatoryMap(List<ClinicalData> data, Map<String, Set<CRFDefinition>> crfDefinitionSet) {
        HashMap<String, Set<String>> mandatoryMap = new HashMap<>();
        data.stream().forEach(clinicalData -> {
            String eventName = clinicalData.getEventName();
            String crfName = clinicalData.getCrfName();
            String crfVersion = clinicalData.getCrfVersion();
            CRFDefinition matching = getMatchingCrf(eventName, crfName, crfVersion, crfDefinitionSet);
            if (matching != null) { // Missing CRF or Event are  separate errors
                Set<String> expected = matching.determineRequiredItemsNames();
                mandatoryMap.put(crfName + crfVersion, expected);
            }
        });
        return mandatoryMap;
    }

    private CRFDefinition getMatchingCrf(String eventName, String CRFName, String CRfVersion, Map<String, Set<CRFDefinition>> eventMap) {
        Set<CRFDefinition> crfInEvents = eventMap.get(eventName);
        if (crfInEvents == null) {
            return null;
        }
        List<CRFDefinition> matching = crfInEvents.stream()
                .filter(crfDefinition -> crfDefinition.getName().equals(CRFName) && crfDefinition.getVersion().equals(CRfVersion)).collect(Collectors.toList());
        assert matching.size() < 2;
        if (matching.size() == 0) {
            return null;
        } else {
            return matching.get(0);
        }
    }


}
