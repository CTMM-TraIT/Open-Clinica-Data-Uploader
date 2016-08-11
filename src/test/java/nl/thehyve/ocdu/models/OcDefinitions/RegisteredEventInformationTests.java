package nl.thehyve.ocdu.models.OcDefinitions;

import nl.thehyve.ocdu.TestUtils;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by Jacob Rousseau on 20-Jun-2016.
 * Copyright CTMM-TraIT / NKI (c) 2016
 */
public class RegisteredEventInformationTests {

    private static List<StudySubjectWithEventsType> studySubjectWithEventsTypeList;

    private static MetaData metaData;

    private static List<Event> eventList;

    private static List<ClinicalData> clinicalDataList;

    @Test
    public void testEventPresentInOC() {
        Map<String, EventResponseType> eventsRegisteredInOpenClinica =
                RegisteredEventInformation.createEventKeyMap(studySubjectWithEventsTypeList);
        assertEquals(true, eventsRegisteredInOpenClinica.containsKey("EVENTFULEV-00006SE_REPEATINGEVENT3"));
        assertEquals(false, eventsRegisteredInOpenClinica.containsKey("EVENTFULEV-00006SE_REPEATINGEVENT8"));
        assertEquals(true, eventsRegisteredInOpenClinica.containsKey("EVENTFULEVENTFULSITEEVS-00001SE_REPEATINGEVENT1"));
        assertEquals(false, eventsRegisteredInOpenClinica.containsKey("EVENTFULEVENTFULSITEEVS-00001SE_REPEATINGEVENT5"));
    }

    @Test
    public void testCreateEventKeyListFromStudySubjectWithEventsTypeList() {
        List<String> eventKeyList = RegisteredEventInformation.createEventKeyListFromStudySubjectWithEventsTypeList(metaData, studySubjectWithEventsTypeList);
        assertEquals(true, eventKeyList.contains("EVENTFUL\t\tEV-00006\tREPEATING_EVENT\t3"));
        assertEquals(false,eventKeyList.contains("EVENTFUL\t\tEV-00006\tREPEATING_EVENT\t8"));
        assertEquals(true, eventKeyList.contains("EVENTFUL\tEVENTFULSITE\tEVS-00001\tREPEATING_EVENT\t1"));
        assertEquals(false, eventKeyList.contains("EVENTFUL\tEVENTFULSITE\tEVS-00001\tREPEATING_EVENT\t5"));
    }

    @Test
    public void testCreateEventKeyListFromEventList() {
        List<String> eventKeyList = RegisteredEventInformation.createEventKeyListFromEventList(eventList);
        assertEquals(true, eventKeyList.contains("EVENTFUL\tEVENTFULSITE\tEVS-00001\tREPEATING_EVENT\t1"));
    }

    @Test
    public void testCreateEventKeyListFroMClinicalData() {
        Set<String> eventKeyList = RegisteredEventInformation.createEventKeyListFroMClinicalData(clinicalDataList);
        assertEquals(true, eventKeyList.contains("EVENTFUL\tEVENTFULSITE\tEVS-00002\tREPEATING_EVENT\t1"));
    }

    @Test
    public void testDetermineEventsToSchedule() {
        Set<ImmutablePair> patInEv = new HashSet<>();
        patInEv.add(new ImmutablePair("EV-00003", "EVENTFUL"));
        patInEv.add(new ImmutablePair("EV-00005", "REPEATING_EVENT#3"));
        // the next 2 events are already present in the listAllByStudyResponse.xml file
        patInEv.add(new ImmutablePair("EVS-00001", "EVENTFUL"));
        patInEv.add(new ImmutablePair("EV-00006", "REPEATING_EVENT#2"));
        Collection<Event> eventsToScheduleList =
                RegisteredEventInformation.determineEventsToSchedule(metaData, studySubjectWithEventsTypeList, patInEv);

        assertEquals(2, eventsToScheduleList.size());
        Event expected = new Event();

        expected.setSsid("EV-00003");
        expected.setEventName("EVENTFUL");
        expected.setRepeatNumber("1");
        assertEquals(true, eventsToScheduleList.contains(expected));

        expected = new Event();
        expected.setSsid("EV-00005");
        expected.setEventName("REPEATING_EVENT");
        expected.setRepeatNumber("3");
        assertEquals(true, eventsToScheduleList.contains(expected));
    }

    @BeforeClass
    public static void setup() throws Exception {
        studySubjectWithEventsTypeList = TestUtils.createStudySubjectWithEventList();
        metaData = new MetaData();
        EventDefinition eventDefinitionRepeatingEvent = new EventDefinition();
        eventDefinitionRepeatingEvent.setStudyEventOID("SE_REPEATINGEVENT");
        eventDefinitionRepeatingEvent.setName("REPEATING_EVENT");

        EventDefinition eventDefinitionEvent = new EventDefinition();
        eventDefinitionEvent.setStudyEventOID("SE_EVENTFUL");
        eventDefinitionEvent.setName("EVENTFUL");


        List<EventDefinition> eventDefinitionList = new ArrayList<>();
        eventDefinitionList.add(eventDefinitionRepeatingEvent);
        eventDefinitionList.add(eventDefinitionEvent);


        metaData.setEventDefinitions(eventDefinitionList);

        eventList = new ArrayList<>();
        Event event = new Event();
        event.setSsid("EVS-00001");
        event.setStudy("EVENTFUL");
        event.setSite("EVENTFULSITE");
        event.setEventName("REPEATING_EVENT");
        event.setRepeatNumber("1");
        eventList.add(event);

        clinicalDataList = new ArrayList<>();
        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setSsid("EVS-00002");
        clinicalData.setStudy("EVENTFUL");
        clinicalData.setSite("EVENTFULSITE");
        clinicalData.setEventName("REPEATING_EVENT");
        clinicalData.setEventRepeat("1");
        clinicalDataList.add(clinicalData);

    }
}
