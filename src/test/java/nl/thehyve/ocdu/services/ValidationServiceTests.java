package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.TestUtils;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by jacob on 8/11/16.
 */
public class ValidationServiceTests {

    private List<StudySubjectWithEventsType> studySubjectWithEventsTypeList;
    private List<Event> eventList;
    private List<ClinicalData> clinicalDataList;
    private MetaData metaData;

    @Before
    public void setUp() throws Exception {
        studySubjectWithEventsTypeList = TestUtils.createStudySubjectWithEventList();

        // The test-subject ( EVS-00001 ) has 3 repeats of the event SE_REPEATINGEVENT as registered in the
        // studySubjectWithEventsTypeList
        String eventRepeat = "4";

        Event event = new Event();
        event.setStudy("Eventful");
        event.setSite("EventfulSite");
        event.setEventName("RepeatingEvent");
        event.setSsid("EVS-00002");
        event.setLocation("Location");
        event.setRepeatNumber(eventRepeat);

        eventList = new ArrayList<>();
        eventList.add(event);
        metaData = new MetaData();
        EventDefinition eventDefinition = new EventDefinition();
        eventDefinition.setStudyEventOID("SE_REPEATINGEVENT");
        eventDefinition.setName("RepeatingEvent");
        metaData.addEventDefinition(eventDefinition);

        eventDefinition = new EventDefinition();
        eventDefinition.setStudyEventOID("SE_EVENTFUL");
        eventDefinition.setName("Non-repeating Event");
        metaData.addEventDefinition(eventDefinition);

        clinicalDataList = new ArrayList<>();
        ClinicalData clinicalData = event.createClinicaData();
        clinicalDataList.add(clinicalData);
    }


    @Test
    /**
     * Tests the business logic for the check for misssing events:
     * <ol>
     *      <li>TestCase 1: event not present in OpenClinica (studySubjectWithEventsTypeList), present in the data (clinicaldatalist) and present in the event defintion list. This is the
     *                      correct situation</li>
     *      <li>TestCase 2: event present in OpenClinica (studySubjectWithEventsTypeList), present in the data (clinicaldatalist) and present in the event definition list. This could
     *                      be considered a use-case when the users want both to overwrite event-data and the clinical data.</li>
     *      <li>TestCase 3: event not present in OpenClinica, present in the data but missing in the event definition list</li>
     *      <li>TestCase 4: eventList empty, event present in data and missing in OpenClinica</li>
     * </ol>
     */
    public void testCheckForMissingEventsInEventDataAndOpenClinica_Repeating() {

        ValidationService validationService = new ValidationService();
        ValidationErrorMessage validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);

        // TestCase 1
        assertEquals(null, validationErrorMessage);

        // TestCase 2:
        Event event = eventList.get(0);
        event.setRepeatNumber("3");
        ClinicalData clinicalData = clinicalDataList.get(0);
        clinicalData.setEventRepeat("3");
        validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);
        assertEquals(null, validationErrorMessage);

        // TestCase 3:
        event = eventList.get(0);
        event.setRepeatNumber("40");
        clinicalData = clinicalDataList.get(0);
        clinicalData.setEventRepeat("4");
        validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);
        assertThat(validationErrorMessage.getMessage(), containsString("The (repeated) events indicated in the event registration file do not match the events present in the data file"));

        // TestCase 4
        eventList.clear();
        validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);
        assertThat(validationErrorMessage.getMessage(), containsString("The (repeated) events indicated in the event registration file do not match the events present in the data file"));
    }

    @Test
    public void testCheckForMissingEventsInEventDataAndOpenClinica_Non_Repeating() {
        // repeat the test-cases but this time for a non-repeating event.
        Event event = eventList.get(0);
        event.setEventName("Non-repeating Event");
        event.setRepeatNumber("");

        ClinicalData clinicalData = clinicalDataList.get(0);
        clinicalData.setEventName("Non-repeating Event");
        clinicalData.setEventRepeat("");

        ValidationService validationService = new ValidationService();
        ValidationErrorMessage validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);

        // TestCase 1
        assertEquals(null, validationErrorMessage);

        // TestCase 2 is skipped

        // Testcase 3
        event = eventList.get(0);
        event.setEventName("RepeatingEvent");
        event.setRepeatNumber("3");
        validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);
        assertThat(validationErrorMessage.getMessage(), containsString("The (repeated) events indicated in the event registration file do not match the events present in the data file"));
        // TestCase 4
        eventList.clear();
        validationErrorMessage =
                validationService.checkForMissingEventsInEventDataAndOpenClinica(metaData, studySubjectWithEventsTypeList, eventList, clinicalDataList);
        assertThat(validationErrorMessage.getMessage(), containsString("The (repeated) events indicated in the event registration file do not match the events present in the data file"));
    }
}
