package nl.thehyve.ocdu.models.OcDefinitions;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openclinica.ws.beans.*;

import java.util.*;

/**
 * Class responsible for providing a {@link Map} with which a check can be performed if an event / event-repeat
 * is present in OpenClinica. Uses a list {@link StudySubjectWithEventsType} which is retrieved from OpenClinica and
 * converts it to a Map with a key consisting of the study identifier, the site identifier (optional), the eventOID
 * and the event-repeat number.
 * Created by Jacob Rousseau on 20-Jun-2016.
 * Copyright CTMM-TraIT / NKI (c) 2016
 */
public class RegisteredEventInformation {

    /**
     * Creates a map of {@link EventResponseType}s with as String as key which can be used to check if an event is present in OpenClinica. The value
     * set contains the event. The key consists of the study identifier, the site identifier (optional), the eventOID and the event
     * repeat number.
     *
     * @param studySubjectWithEventsTypeList
     * @return
     */
    public static Map<String, EventResponseType> createEventKeyMap(List<StudySubjectWithEventsType> studySubjectWithEventsTypeList) {
        Map<String, EventResponseType> ret = new HashMap<>(studySubjectWithEventsTypeList.size());
        for (StudySubjectWithEventsType studySubjectWithEventsType : studySubjectWithEventsTypeList) {
            EventsType eventsTypeList = studySubjectWithEventsType.getEvents();
            List<EventResponseType> eventList = eventsTypeList.getEvent();
            String subjectLabel = studySubjectWithEventsType.getLabel();
            for (EventResponseType eventResponseType : eventList) {
                StringBuffer buffer = new StringBuffer();
                StudyRefType studyRefType = studySubjectWithEventsType.getStudyRef();
                buffer.append(studyRefType.getIdentifier());
                SiteRefType siteRefType = studyRefType.getSiteRef();
                if (siteRefType != null) {
                    buffer.append(siteRefType.getIdentifier());
                }
                buffer.append(subjectLabel);
                buffer.append(eventResponseType.getEventDefinitionOID());
                buffer.append(eventResponseType.getOccurrence());
                ret.put(buffer.toString().toUpperCase(), eventResponseType);
            }
        }
        return ret;
    }

    public static Set<String> createEventKeyListFromClinicalData(List<ClinicalData> clinicalDataList) {
        Set<String> ret = new HashSet<>();
        for (ClinicalData clinicalData : clinicalDataList) {
            ret.add(clinicalData.createEventKey());
        }
        return ret;
    }

    public static Set<String> createEventKeyListFromStudySubjectWithEventsTypeList(MetaData metaData, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList) {
        Map<String, String> eventOIDToNameMap = createEventOIDToNameMap(metaData);
        Set<String> ret = new HashSet<>();
        for (StudySubjectWithEventsType studySubjectWithEventsType : studySubjectWithEventsTypeList) {
            String ssid = studySubjectWithEventsType.getLabel();
            String study = studySubjectWithEventsType.getStudyRef().getIdentifier();
            String site = studySubjectWithEventsType.getStudyRef().getSiteRef() != null ? studySubjectWithEventsType.getStudyRef().getSiteRef().getIdentifier() : "";
            for (EventResponseType event : studySubjectWithEventsType.getEvents().getEvent()) {
                ClinicalData clinicalData = new ClinicalData();
                clinicalData.setSsid(ssid);
                clinicalData.setStudy(study);
                clinicalData.setSite(site);
                String eventName = eventOIDToNameMap.get(event.getEventDefinitionOID());
                clinicalData.setEventName(eventName);
                clinicalData.setEventRepeat(event.getOccurrence());
                ret.add(clinicalData.createEventKey());
            }
        }
        return ret;
    }

    /**
     * Creates list of String which identify the events present in a list of Events.
     *
     * @param eventList
     * @return
     */
    public static List<String> createEventKeyListFromEventList(List<Event> eventList) {
        List<String> ret = new  ArrayList<>();
        for (Event event : eventList) {
            ClinicalData clinicalData = event.createClinicaData();
            clinicalData.setStudy(event.getStudy());
            clinicalData.setSsid(event.getSsid());;
            clinicalData.setSite(event.getSite());
            clinicalData.setEventName(event.getEventName());
            clinicalData.setEventRepeat(event.getRepeatNumber());
            ret.add(clinicalData.createEventKey());
        }
        return ret;
    }

    public static Map<String, String> createEventOIDToNameMap(MetaData metaData) {
        Map<String, String> eventOIDToNameMap = new HashMap<>();
        metaData.getEventDefinitions().forEach(eventDefinition -> {
            eventOIDToNameMap.put(eventDefinition.getStudyEventOID(), eventDefinition.getName());
        });
        return eventOIDToNameMap;
    }

    public static Collection<Event> determineEventsToSchedule(MetaData metaData,
                                                              List<StudySubjectWithEventsType> studySubjectWithEventsTypeList,
                                                              Set<ImmutablePair> patientsInEvent) {
        Collection<Event> ret = new HashSet<>();

        Map<String, String> eventOIDToNameMap = createEventOIDToNameMap(metaData);

        Collection<Event> alreadyRegistered = new HashSet<>();
        for (StudySubjectWithEventsType studySubjectWithEventsType : studySubjectWithEventsTypeList) {
            List<EventResponseType> regEvents = studySubjectWithEventsType.getEvents().getEvent();
            String studySubjectID = studySubjectWithEventsType.getLabel();
            for (EventResponseType eventResponseType : regEvents) {
                String eventOID = eventResponseType.getEventDefinitionOID();
                String eventName = eventOIDToNameMap.get(eventOID);
                String eventRepeatNumber = eventResponseType.getOccurrence();
                Event event = new Event();
                event.setEventName(eventName);
                event.setRepeatNumber(eventRepeatNumber);
                event.setSsid(studySubjectID);
                alreadyRegistered.add(event);
            }
        }

        patientsInEvent.stream().forEach(patientInEvent -> {
            String studySubjectID = (String) patientInEvent.left;
            String eventName = (String) patientInEvent.right;
            String eventRepeatNumber = StringUtils.substringAfterLast(eventName, "#");
            if (StringUtils.isEmpty(eventRepeatNumber)) {
                eventRepeatNumber = "1";
            }
            String eventSiteCombination = StringUtils.substringBeforeLast(eventName, "#");
            eventName = StringUtils.substringAfterLast(eventSiteCombination, "#");
            String site = StringUtils.substringBefore(eventSiteCombination, "#");
            Event eventToSchedule = new Event();
            eventToSchedule.setSite(site);
            eventToSchedule.setRepeatNumber(eventRepeatNumber);
            eventToSchedule.setSsid(studySubjectID);
            eventToSchedule.setEventName(eventName);
            if (! alreadyRegistered.contains(eventToSchedule)) {
               ret.add(eventToSchedule);
            }
        });
        return ret;
    }
}
