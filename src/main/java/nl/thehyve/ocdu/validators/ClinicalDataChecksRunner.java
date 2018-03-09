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

package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.clinicalDataChecks.ClinicalDataCrossCheck;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for executing cross checks on clinical data submitted by the user.
 * in order to set which checks are to be run use setChecks, passing instances
 * of ClinicalDataCrossCheck.
 *
 * Created by piotrzakrzewski on 22/06/16.
 */
public class ClinicalDataChecksRunner {

    private Collection<ClinicalDataCrossCheck> checks = new ArrayList<>();

    private Collection<ClinicalDataCrossCheck> checksOverSubjects = new ArrayList<>();

    public Collection<ClinicalDataCrossCheck> getChecks() {
        return checks;
    }

    public void setChecks(Collection<ClinicalDataCrossCheck> checks) {
        this.checks = checks;
    }


    public Collection<ClinicalDataCrossCheck> getChecksOverSubjects() {
        return checksOverSubjects;
    }

    public void setChecksOverSubjects(Collection<ClinicalDataCrossCheck> checksOverSubjects) {
        this.checksOverSubjects = checksOverSubjects;
    }

    private final List<ClinicalData> clinicalData;
    private final MetaData metadata;
    /**
     * The list of the current event and crf status of all the subjects present in a study.
     */
    private final List<StudySubjectWithEventsType> subjectWithEventsTypeList;

    public ClinicalDataChecksRunner(MetaData metadata, List<ClinicalData> clinicalData,
                                    List<StudySubjectWithEventsType> subjectWithEventsTypes) {
        this.clinicalData = clinicalData;
        this.metadata = metadata;
        this.subjectWithEventsTypeList = subjectWithEventsTypes;
    }

    public List<ValidationErrorMessage> getErrors() {
        // Map to combine all the error messages over subjects into one message per error type.
        // The key is the class name of the error.
        Map<String, ValidationErrorMessage> classErrorMessageMap = new HashMap<>();
        Map<Long, List<ClinicalData>> splitDataPerLine = splitDataPerLine(clinicalData);
        for (List<ClinicalData> clinicalDataPerSubjectEvent : splitDataPerLine.values()) {
            List<ValidationErrorMessage> errorList = performValidation(clinicalDataPerSubjectEvent, checks);
            for (ValidationErrorMessage error : errorList) {
                String className = error.getClass().getName();
                ValidationErrorMessage combinedValidationErrorMessage = null;
                if (classErrorMessageMap.containsKey(className)) {
                    combinedValidationErrorMessage = classErrorMessageMap.get(className);
                    for (String message : error.getOffendingValues()) {
                        combinedValidationErrorMessage.addOffendingValue(message);
                    }
                }
                else {
                    combinedValidationErrorMessage = error;
                    classErrorMessageMap.put(className, combinedValidationErrorMessage);
                }
            }
        }
        // The scope of certain validations (e.g. SsidUniqueCrossCheck) is over the entire data set. A separate
        // list is needed following the requirement that the show/hide functionality only works on a single
        // Subject/Event row. This corresponds to a single line in the source data-file.
        List<ValidationErrorMessage> errorListOverSubjects = performValidation(clinicalData, checksOverSubjects);
        errorListOverSubjects.addAll(classErrorMessageMap.values());
        return errorListOverSubjects;
    }

    public List<ClinicalData> getClinicalData() {
        return clinicalData;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public List<StudySubjectWithEventsType> getSubjectWithEventsTypeList() {
        return subjectWithEventsTypeList;
    }

    public Map<ClinicalData, ItemDefinition> buildItemDefMap(List<ClinicalData> data, MetaData metaData) {
        Map<ClinicalData, ItemDefinition> itemDefMap = new HashMap<>();
        data.forEach(clinicalData -> {
            ItemDefinition itemDefinition = getMatching(clinicalData, metaData);
            if (itemDefinition != null) {
                itemDefMap.put(clinicalData, itemDefinition);
            }
        });
        return itemDefMap;
    }

    private List<ValidationErrorMessage> performValidation(List<ClinicalData> clinicalDataToValidate,
                                                           Collection<ClinicalDataCrossCheck> crossCheckList) {
        List<ValidationErrorMessage> errorList = new ArrayList<>();
        Map<ClinicalData, ItemDefinition> defMap = buildItemDefMap(clinicalDataToValidate, metadata);
        Map<ClinicalData, Boolean> showMap = buildShownMap(defMap);
        Map<String, Set<CRFDefinition>> eventMap = buildEventMap(metadata);
        for (ClinicalDataCrossCheck clinicalDataCrossCheck : crossCheckList) {
            ValidationErrorMessage error = clinicalDataCrossCheck.getCorrespondingError(clinicalDataToValidate, metadata,
                    defMap, subjectWithEventsTypeList, showMap, eventMap);
            if (error != null) {
                errorList.add(error);
            }
        }
        return errorList;
    }

    private CRFDefinition getMatchingCrf(String eventName, String CRFName, String CRfVersion, MetaData metaData) {
        Map<String, Set<CRFDefinition>> eventMap = buildEventMap(metaData);
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

    private ItemDefinition getMatching(ClinicalData dataPoint, MetaData metaData) {
        CRFDefinition matchingCrf = getMatchingCrf(dataPoint.getEventName(), dataPoint.getCrfName(), dataPoint.getCrfVersion(), metaData);
        if (matchingCrf == null) {
            return null;
        }
        Set<ItemDefinition> itemDefinitions = matchingCrf.allItems();
        List<ItemDefinition> matchingItems = itemDefinitions.stream()
                .filter(itemDefinition -> itemDefinition.getName().equals(dataPoint.getItem()))
                .collect(Collectors.toList());
        assert matchingItems.size() < 2;
        if (matchingItems.size() == 0) return null;
        else return matchingItems.get(0);
    }

    private Map<String, Set<CRFDefinition>> buildEventMap(MetaData metaData) {
        Map<String, Set<CRFDefinition>> eventMap = new HashMap<>();
        metaData.getEventDefinitions().stream().forEach(eventDefinition ->
                {
                    Set<CRFDefinition> crfNames = eventDefinition.getCrfDefinitions()
                            .stream()
                            .collect(Collectors.toSet());
                    eventMap.put(eventDefinition.getName(), crfNames);
                }
        );
        return eventMap;
    }

    /*
     * Shown/hidden status is context dependant - it is valid only in context of other data-points for given patient.
     * This is why hidden/shown is not a field of ClinicalData.
     * */
    private Map<ClinicalData, Boolean> buildShownMap(Map<ClinicalData, ItemDefinition> definitionMap) {
        Map<ClinicalData, Boolean> shownMap = new HashMap<>();
        Set<ClinicalData> clinicalDataList = definitionMap.keySet();
        Map<Long, List<ClinicalData>> splitPerLineMap = splitDataPerLine(clinicalDataList);
        for (List<ClinicalData> listPerLine : splitPerLineMap.values()) {
            VisibleStateDeterminator visibleStateDeterminator =
                    new VisibleStateDeterminator(listPerLine, definitionMap);
            listPerLine.forEach(clinicalDataToCheck -> {
                boolean shown = visibleStateDeterminator.determineShown(clinicalDataToCheck, metadata);
                shownMap.put(clinicalDataToCheck, shown);
            });
        }
        return shownMap;
    }

    /**
     * Splits a {@link List} of {@link ClinicalData} into separate lists, one per line.
     * Required because the scope of show/hide functionality is always per line.
     * @param data the total list spanning multiple subjects / rows
     * @return a map per line with the line number as key
     */
    private Map<Long, List<ClinicalData>> splitDataPerLine(Collection<ClinicalData> data) {
        Map<Long, List<ClinicalData>> ret = new HashMap<>();
        for (ClinicalData clinicalData : data) {
            List<ClinicalData> splitList = ret.get(clinicalData.getLineNumber());
            if (splitList == null) {
                splitList = new ArrayList<>();
                ret.put(clinicalData.getLineNumber(), splitList);
            }
            splitList.add(clinicalData);
        }
        return ret;
    }
}
