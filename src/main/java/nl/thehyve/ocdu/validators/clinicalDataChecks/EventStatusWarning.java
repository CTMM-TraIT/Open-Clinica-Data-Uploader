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
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.EventStatusWarningForOverwrite;
import nl.thehyve.ocdu.models.errors.MessageType;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validator to find events present in the data for which the status is <code>data entry started</code> or
 * <code>data entry completed</code> in OpenClinica. Required to notify the user of this situation.
 * User then either disregard these warnings and optionally proceed with the upload.
 * Created by jacob on 9/13/16.
 */
public class EventStatusWarning implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        ValidationErrorMessage error = new EventStatusWarningForOverwrite();
        error.setMessageType(MessageType.WARNING);
        Set<String> subjectsInData = data.stream().map(clinicalData -> clinicalData.getSsid()).collect(Collectors.toSet());
        Set<String> crfNamesInData = data.stream().map(clinicalData -> clinicalData.getCrfName()).collect(Collectors.toSet());
        Set<String> offenders = new HashSet<>();
        Set<String> eventsInData = eventMap.keySet();
        Map<String, String> eventOidToEventNameMap = eventOIDsInData(metaData, eventsInData);
        studySubjectWithEventsTypeList.stream().forEach(studySubjectWithEventsType -> {
            if (subjectsInData.contains(studySubjectWithEventsType.getLabel())) {
                EventsType eventsWrapper = studySubjectWithEventsType.getEvents();
                List<EventResponseType> events = eventsWrapper.getEvent();
                events.stream().forEach(eventResponseType -> {
                    String eventStatus = eventResponseType.getStatus();
                    if (hasValidEventStatus(eventStatus)) {
                        List<EventCrfInformationList> eventCrfInformationListList = eventResponseType.getEventCrfInformation();
                        eventCrfInformationListList.stream().forEach(eventCrfInformationList -> {
                            List<EventCrfType> eventCRFList = eventCrfInformationList.getEventCrf();
                            eventCRFList.stream().forEach(eventCrfType -> {
                                if (crfNamesInData.contains(eventCrfType.getName())) {
                                    String status = eventCrfType.getStatus();
                                    if (hasStatusToWarnFor(status)) {
                                        offenders.add("Subject " + ClinicalData.CD_SEP_PREFIX + studySubjectWithEventsType.getLabel() + ClinicalData.CD_SEP_POSTEFIX
                                                + "Event " + ClinicalData.CD_SEP_PREFIX + eventOidToEventNameMap.get(eventResponseType.getEventDefinitionOID()) + ClinicalData.CD_SEP_POSTEFIX
                                                + "CRF " + ClinicalData.CD_SEP_PREFIX + eventCrfType.getName() + ClinicalData.CD_SEP_POSTEFIX
                                                + "has status: " + ClinicalData.CD_SEP_PREFIX + status + ClinicalData.CD_SEP_POSTEFIX);
                                    }
                                }
                            });
                        });
                    }
                });
            }
        });
        error.addAllOffendingValues(offenders);
        if (error.getOffendingValues().size() > 0) {
            return error;
        }
        return null;
    }

    private boolean hasValidEventStatus(String eventStatus) {
        return eventStatus.equals("available");
    }

    private boolean hasStatusToWarnFor(String status) {
        if (status.equals("initial data entry") || status.equals("data entry complete")) {
            return true;
        }
        return false;
    }

    private Map<String, String> eventOIDsInData(MetaData metaData, Set<String> usedEventNames) {
        Map<String, String> eventOIDToNameMap = new HashMap<>();
        for (EventDefinition eventDefinition : metaData.getEventDefinitions()) {
            if (usedEventNames.contains(eventDefinition.getName())) {
                eventOIDToNameMap.put(eventDefinition.getStudyEventOID(), eventDefinition.getName());
            }
        }
        return eventOIDToNameMap;
    }
}
