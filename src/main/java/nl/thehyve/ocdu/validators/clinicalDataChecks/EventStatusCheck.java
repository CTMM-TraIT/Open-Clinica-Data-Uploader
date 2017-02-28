package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.EventStatusNotAllowed;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.EventCrfInformationList;
import org.openclinica.ws.beans.EventCrfType;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class EventStatusCheck implements ClinicalDataCrossCheck {

    private static final Logger log = LoggerFactory.getLogger(EventStatusCheck.class);

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        ValidationErrorMessage error = new EventStatusNotAllowed();
        Set<String> offenders = new HashSet<>();
        Set<String> eventsInData = eventMap.keySet();
        Set<String> subjectIDSetWithError = new HashSet<>();

        Set<String> eventKeySet = RegisteredEventInformation.createEventKeyListFromClinicalData(data);
        Map<String, String> eventOidToEventNameMap = eventOIDsInData(metaData, eventsInData);
        Map<String, EventResponseType> eventResponseTypeMap = RegisteredEventInformation.createEventKeyMapForComparisonWithEventResponseType(studySubjectWithEventsTypeList, metaData);

        for (String eventKey : eventKeySet) {
            EventResponseType eventResponseType = eventResponseTypeMap.get(eventKey);
            if (eventResponseType != null) {
                String subjectEventStatus = eventResponseType.getSubjectEventStatus();
                String eventStatus = eventResponseType.getStatus();
                String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(eventKey, "\t");
                final String subjectID;
                if (parts.length > 2) {
                    subjectID = parts[2];
                }
                else {
                    subjectID = "Unknown";
                }
                if (hasValidEventStatus(eventStatus, subjectEventStatus)) {
                    List<EventCrfInformationList> eventCrfInformationListList = eventResponseType.getEventCrfInformation();
                    eventCrfInformationListList.stream().forEach(eventCrfInformationList -> {
                        List<EventCrfType> eventCRFList = eventCrfInformationList.getEventCrf();
                        eventCRFList.stream().forEach(eventCrfType -> {
                            String status = eventCrfType.getStatus();
                            if (isInvalidStatus(status)) {
                                subjectIDSetWithError.add(subjectID);
                                offenders.add("Subject " + ClinicalData.CD_SEP_PREFIX + subjectID + ClinicalData.CD_SEP_POSTEFIX
                                        + "Event " + ClinicalData.CD_SEP_PREFIX + eventOidToEventNameMap.get(eventResponseType.getEventDefinitionOID()) + ClinicalData.CD_SEP_POSTEFIX
                                        + "CRF " + ClinicalData.CD_SEP_PREFIX + eventCrfType.getName() + ClinicalData.CD_SEP_POSTEFIX
                                        + "has status: " + ClinicalData.CD_SEP_PREFIX + status + ClinicalData.CD_SEP_POSTEFIX);
                            }
                        });
                    });
                }
                else {
                    subjectIDSetWithError.add(subjectID);
                    if (("removed".equals(eventStatus)) ||
                        ("auto-removed".equals(eventStatus))) {
                        subjectEventStatus = "removed";
                    }
                    offenders.add("Subject " + ClinicalData.CD_SEP_PREFIX + subjectID + ClinicalData.CD_SEP_POSTEFIX +
                                   "contains an event-status or CRF-status which does not allow uploading; status : "
                                    + subjectEventStatus);
                }
            }
            else {
                log.info("Unable to find matching event response.");
            }
        }

        error.addAllOffendingValues(offenders);
        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, subjectIDSetWithError, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else
            return null;
    }

    private boolean hasValidEventStatus(String eventStatus, String subjectEventStatus) {
        if ("signed".equals(subjectEventStatus)) {
            return false;
        }
        return "available".equals(eventStatus);
    }

    private boolean isInvalidStatus(String status) {
        if (! (status.equals("available") || status.equals("unavailable") || status.equals("data entry started") || status.equals("data entry complete") || status.equals("initial data entry"))) {
            return true;
        } else return false;
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
