package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

public class EventDataOcChecksTests {

    MetaData metadata;
    Event event;
    List<Event> noEvents = Collections.emptyList();

    @Test
    public void testSuccess() {
        EventDataOcChecks checks = new EventDataOcChecks(metadata, noEvents);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, empty());
    }

    @Test
    public void testSuccessWithMinimumRequiredFields() {
        event.setSite(null);
        event.setLocation(null);
        event.setStartTime(null);
        event.setEndDate(null);
        event.setEndTime(null);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, empty());
    }

    @Test
    public void testSubjectIdIsRequired() {
        event.setSsid(" ");

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Subject id has to be specified."))
        ));
    }

    @Test
    public void testEventNameIdIsRequired() {
        event.setEventName(" ");
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Event name has to be specified."))
        ));
    }

    @Test
    public void testStudyNameIsRequired() {
        event.setStudy(" ");
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Study name has to be specified."))
        ));
    }

    @Test
    public void testStudyIsNull() {

        event.setStudy(null);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);


        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Study name has to be specified."))
        ));
    }

    @Test
    public void testStartDateIsRequired() {
        event.setStartDate(" ");
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Start date has to be specified."))
        ));
    }

    @Test
    public void testRepeatNumberIsRequired() {
        event.setRepeatNumber(" ");
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(
                hasProperty("message", is("Repeat number has to be specified."))
        ));
    }

    @Test
    public void testEventHasToExist() {
        String eventName = "Un-existing Event";
        event.setEventName(eventName);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);


        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Event does not exist")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, value: " + eventName))
        )));
    }

    @Test
    public void testStudyHasToExist() {
        String studyName = "Un-existing Study";
        event.setStudy(studyName);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);


        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Study identifier in your event registration file does not match study identifier in your data file. Expected: Test Study")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, value: "  +  studyName))
        )));
    }

    @Test
    public void testSiteHasToExist() {
        String siteName = "Un-existing Site";
        event.setSite(siteName);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Site does not exist. Use the Unique Protocol ID of the site(s)")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, value: " + siteName ))
        )));
    }

    @Test
    public void testLocationStringLength() {
        int maxLocationNameLength = EventDataOcChecks.LOCATION_STRING_MAX_LENGTH;
        int violationLocationNameLength = maxLocationNameLength + 1;
        String tooLongLocationName = String.format("%1$" + violationLocationNameLength + "s", "L");
        event.setLocation(tooLongLocationName);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Location name is too long. " +
                        "It has not to exceed " + maxLocationNameLength + " character in length.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, value: " + tooLongLocationName))
        )));
    }

    @Test
    public void testWrongStartDateFormat() {
        String wrongStartDate = "2004-02-21";
        event.setStartDate(wrongStartDate);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Start date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 2004-02-21"))
        )));
    }

    @Test
    public void testInvalidStartDate() {
        String invalidStartDate = "31-02-2013";
        event.setStartDate(invalidStartDate);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Start date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 31-02-2013"))
        )));
    }

    @Test
    public void testWrongEndDateFormat() {
        String wrongStartDate = "2004-02x-21";
        event.setEndDate(wrongStartDate);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("End date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 2004-02x-21"))
        )));
    }

    @Test
    public void testInvalidEndDate() {
        String invalidStartDate = "31-02-1977";
        event.setEndDate(invalidStartDate);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("End date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 31-02-1977"))
        )));
    }

    @Test
    public void testWrongStartTimeFormat() {
        String wrongStartTime = "12:00AM";
        event.setStartTime(wrongStartTime);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Start time is invalid. The time format should be hh:mm. For example, 13:29.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 12:00AM"))
        )));
    }

    @Test
    public void testInvalidStartTime() {
        String invalidStartDate = "24:00";
        event.setStartTime(invalidStartDate);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Start time is invalid. The time format should be hh:mm. For example, 13:29.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 24:00"))
        )));
    }

    @Test
    public void testWrongEndTimeFormat() {
        String wrongEndTime = "12:00AM";
        event.setEndTime(wrongEndTime);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("End time is invalid. The time format should be hh:mm. For example, 13:29.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 12:00AM"))
        )));
    }

    @Test
    public void testInvalidEndTime() {
        String invalidEndDate = "24:00";
        event.setEndTime(invalidEndDate);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("End time is invalid. The time format should be hh:mm. For example, 13:29.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, 24:00"))
        )));
    }

    @Test
    public void testInvalidDateRange() {
        String startDate = "22-02-2014";
        String endDate = "21-02-2014";
        event.setStartDate(startDate);
        event.setEndDate(endDate);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Date range is invalid.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, start date: 22-02-2014, end date: 21-02-2014. End date is before start date"))
        )));
    }

    @Test
    public void testInvalidTimeRange() {
        String date = "22-02-2014";
        String startTime = "10:00";
        String endTime = "7:00";
        event.setStartDate(date);
        event.setStartTime(startTime);
        event.setEndDate(date);
        event.setEndTime(endTime);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("End time is before start time.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, start time: 10:00, end time: 7:00"))
        )));
    }

    @Test
    public void testRepeatNumberWrongFormat() {
        String repeatNumber = "one";
        event.setRepeatNumber(repeatNumber);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Repeat number is not a positive number.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, one"))
        )));
    }

    @Test
    public void testRepeatNumberBlank() {
        String repeatNumber = "";
        event.setRepeatNumber(repeatNumber);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);

        EventDataOcChecks checks = new EventDataOcChecks(metadata, eventList);
        List<ValidationErrorMessage> errors = checks.validate();

        assertThat(errors, contains(allOf(
                hasProperty("message", is("Repeat number has to be specified.")),
                hasProperty("offendingValues", contains("Line number: 1, subject: subj1, value: Empty"))
        )));
    }

    @Test
    public void testDuplicatedEvents() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);

        String anotherEventName = event.getEventName() + "(another)";
        EventDefinition anotherEventDefinition = new EventDefinition();
        anotherEventDefinition.setName(anotherEventName);
        metadata.getEventDefinitions().add(anotherEventDefinition);
        Event event1 = new Event();
        event1.setStudy(event.getStudy());
        event1.setEventName(anotherEventName);
        event1.setSsid(event.getSsid());
        event1.setStartDate("10-06-2014");
        event1.setRepeatNumber("2");
        event1.setLineNumber(4);

        events.add(event1);
        events.add(event1);


        EventDataOcChecks checks = new EventDataOcChecks(metadata, events);
        List<ValidationErrorMessage> errors = checks.getErrors();

        assertThat(errors, contains(
                        hasProperty("message", is("An event for the given subject is duplicated."))
        ));

        assertThat(errors, contains(
            allOf(
                    hasProperty("offendingValues", contains("Line number: 4, subject: subj1 value(s): [subj1, Test Event(another), 2]"))
            )
        ));
    }

    @Before
    public void setUp() throws Exception {
        String sid = "subj1";
        String studyName = "Test Study";
        String studyProtocolName = "Test Study";
        String eventName = "Test Event";

        String siteUniqueID = "TestSiteUniqueID";

        metadata = new MetaData();
        metadata.setStudyName(studyName);
        ArrayList<EventDefinition> eventDefinitions = new ArrayList<>();
        EventDefinition eventDefinition = new EventDefinition();
        eventDefinition.setName(eventName);
        eventDefinitions.add(eventDefinition);
        metadata.setEventDefinitions(eventDefinitions);
        ArrayList<SiteDefinition> siteDefinitions = new ArrayList<>();
        SiteDefinition siteDefinition = new SiteDefinition();
        siteDefinition.setName(siteUniqueID);
        siteDefinition.setUniqueID(siteUniqueID);
        siteDefinitions.add(siteDefinition);
        metadata.setSiteDefinitions(siteDefinitions);
        metadata.setProtocolName(studyProtocolName);

        event = new Event();
        event.setStudy(studyProtocolName);
        event.setStudyProtocolName(studyProtocolName);
        event.setEventName(eventName);
        event.setSite(siteUniqueID);
        event.setSsid(sid);
        event.setStartDate("22-02-2014");
        event.setStartTime("0:00");
        event.setEndDate("23-02-2014");
        event.setEndTime("23:59");
        event.setRepeatNumber("2");
        event.setLocation("Test Location");
        event.setLineNumber(1);
    }

    @Test
    public void locationBannedTest() throws Exception {
        metadata.setLocationRequirementSetting(ProtocolFieldRequirementSetting.BANNED);
        List<Event> events = new ArrayList<>();
        events.add(event);
        EventDataOcChecks checks = new EventDataOcChecks(metadata, events);
        List<ValidationErrorMessage> errors = checks.getErrors();
        assertThat(errors, notNullValue());
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0).getMessage(), containsString("Location is not allowed in this study"));
    }

    @Test
    public void locationRequiredTest() throws Exception {
        metadata.setLocationRequirementSetting(ProtocolFieldRequirementSetting.MANDATORY);
        List<Event> events = new ArrayList<>();
        event.setLocation("");
        events.add(event);
        EventDataOcChecks checks = new EventDataOcChecks(metadata, events);
        List<ValidationErrorMessage> errors = checks.getErrors();
        assertThat(errors, notNullValue());
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0).getMessage(), containsString("Location is required"));
    }

    @Test
    public void locationOptionalEmptyTest() throws Exception {
        metadata.setLocationRequirementSetting(ProtocolFieldRequirementSetting.OPTIONAL);
        List<Event> events = new ArrayList<>();
        event.setLocation("");
        events.add(event);
        EventDataOcChecks checks = new EventDataOcChecks(metadata, events);
        List<ValidationErrorMessage> errors = checks.getErrors();
        assertThat(errors, notNullValue());
        assertThat(errors, hasSize(0));
    }

    @Test
    public void locationOptionalFilledTest() throws Exception {
        metadata.setLocationRequirementSetting(ProtocolFieldRequirementSetting.OPTIONAL);
        List<Event> events = new ArrayList<>();
        events.add(event);
        EventDataOcChecks checks = new EventDataOcChecks(metadata, events);
        List<ValidationErrorMessage> errors = checks.getErrors();
        assertThat(errors, notNullValue());
        assertThat(errors, hasSize(0));
    }

}
