package nl.thehyve.ocdu.validators;


import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.EventType;
import org.openclinica.ws.beans.EventsType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventDataOcChecks {

    private static final Logger log = LoggerFactory.getLogger(EventDataOcChecks.class);

    public static final int LOCATION_STRING_MAX_LENGTH = 4000;
    public static final DateFormat SIMPLE_DATE_FORMAT;
    public static final DateFormat SIMPLE_TIME_FORMAT;

    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SIMPLE_DATE_FORMAT.setLenient(false);

        SIMPLE_TIME_FORMAT = new SimpleDateFormat("H:mm", Locale.ENGLISH);
        SIMPLE_TIME_FORMAT.setLenient(false);
    }

    private final List<Event> events;
    private final List<Subject> subjects;
    private final MetaData metadata;
    private final List<StudySubjectWithEventsType> subjectWithEventsTypeList;

    private final Set<String> eventNames;
    private final Set<String> siteNames;

    public EventDataOcChecks(MetaData metadata, List<Event> eventList, List<Subject> subjectList, List<StudySubjectWithEventsType> subjectWithEventsTypeList) {
        this.metadata = metadata;
        this.events = eventList;
        this.subjects = subjectList;
        this.subjectWithEventsTypeList = subjectWithEventsTypeList;


        if (metadata.getEventDefinitions() == null) {
            this.eventNames = Collections.emptySet();
        } else {
            this.eventNames =
                    metadata.getEventDefinitions().stream()
                            .map(EventDefinition::getName)
                            .map(s -> s.toUpperCase())
                            .collect(Collectors.toSet());
        }

        if (metadata.getSiteDefinitions() == null) {
            this.siteNames = Collections.emptySet();
        } else {
            this.siteNames = metadata
                    .getSiteDefinitions()
                    .stream()
                    .map(SiteDefinition::getUniqueID)
                    .collect(Collectors.toSet());
        }
    }

    public List<ValidationErrorMessage> getErrors() {
        List<ValidationErrorMessage> errors = new ArrayList<>();

        errors.addAll(validate());

        return errors;
    }

    protected List<ValidationErrorMessage> validate() {
        ArrayList<ValidationErrorMessage> errors = new ArrayList<>();

        validateEvent(errors, "Subject id has to be specified.", event -> StringUtils.isBlank(event.getSsid()), event -> event.getSsid());

        validateEvent(errors, "Event name has to be specified.", event -> StringUtils.isBlank(event.getEventName()), event -> event.getEventName());

        validateEvent(errors, "Study name has to be specified.", event -> StringUtils.isBlank(event.getStudy()), event -> event.getStudy());

        validateEvent(errors, "Start date has to be specified.", event -> StringUtils.isBlank(event.getStartDate()), event -> event.getStartDate());

        validateEvent(errors, "Repeat number has to be specified.", event -> StringUtils.isBlank(event.getRepeatNumber()), event -> event.getRepeatNumber());

        validateEvent(errors,
                       "Study identifier in your event registration file does not match study identifier " + "in your data file. Expected: " + metadata.getProtocolName(),
                      event -> (StringUtils.isNotBlank(event.getStudy()) && !event.getStudy().equals(metadata.getProtocolName())),
                      event -> event.getStudy());

        validateEvent(errors, "Event does not exist",
                    event -> ((! StringUtils.isBlank(event.getEventName())) && (! eventNames.contains(event.getEventName().toUpperCase()))),
                    event -> event.getEventName());

        validateEvent(errors, "Site does not exist. Use the Unique identifier of the site(s)",
                event -> (StringUtils.isNotBlank(event.getSite()) && !siteNames.contains(event.getSite())),
                event -> event.getSite());

        validateEvent(errors,
                "Location name is too long. It has not to exceed " + LOCATION_STRING_MAX_LENGTH + " character in length.",
                event -> (StringUtils.isNotBlank(event.getLocation()) && event.getLocation().length() > LOCATION_STRING_MAX_LENGTH),
                event -> event.getLocation());

        validateEvent(errors,
                "Location is not allowed in this study, remove the column or leave fields empty",
                event -> (StringUtils.isNotBlank(event.getLocation()) && metadata.getLocationRequirementSetting().equals(ProtocolFieldRequirementSetting.BANNED)),
                event -> event.getLocation());

        validateEvent(errors,
                "Location is required in this study, but one or more of events in your file lack it",
                event -> (StringUtils.isBlank(event.getLocation()) && metadata.getLocationRequirementSetting().equals(ProtocolFieldRequirementSetting.MANDATORY)),
                event -> event.getLocation());

        validateStartDate(errors);
        validateStartTime(errors);
        validateEndDate(errors);
        validateEndTime(errors);
        validateNonEmptyEndTime(errors);
        validateStartEndRanges(errors);
        validateRepeatNumber(errors);
        validateDuplicateEvents(errors);
        validateEventGap(errors);


        return errors;
    }


    private void validateEvent(Collection<ValidationErrorMessage> errorMessageCollection,
                                      String errorMessage,
                                      Predicate<Event> predicate,
                                      Function<Event, String> value) {
        ValidationErrorMessage result = new ValidationErrorMessage(errorMessage);
        for (Event event : events) {
            if (predicate.test(event)) {
                String strValue = value.apply(event);
                if (StringUtils.isBlank(strValue)) {
                    strValue = "Empty";
                }
                result.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", value: " + strValue);
                event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
            }
        }
        if (! result.getOffendingValues().isEmpty()) {
            errorMessageCollection.add(result);
        }
    }

    private void validateDuplicateEvents(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage duplicatedEvent = new ValidationErrorMessage("An event for the given subject is duplicated.");
        Map<List<String>, Event> keyToEventMap = new HashMap<>();
        for (Event event : events) {
            ArrayList<String> key = new ArrayList<>();
            key.add(event.getSsid());
            key.add(event.getEventName());
            key.add(event.getRepeatNumber());
            if (keyToEventMap.containsKey(key)) {
                duplicatedEvent.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + " value(s): " + key);
                event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
            } else {
                keyToEventMap.put(key, event);
            }
        }
        if (! duplicatedEvent.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(duplicatedEvent);
        }

    }

    private void validateRepeatNumber(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage invalidRepeatNumber = new ValidationErrorMessage("Repeat number is not a positive number.");
        for (Event event : events) {
            if (StringUtils.isNotBlank(event.getRepeatNumber())) {
                Optional<Integer> repeatNumberOpt = parseIntOpt(event.getRepeatNumber());
                if (!repeatNumberOpt.isPresent() || repeatNumberOpt.get() < 1) {
                    invalidRepeatNumber.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", " + event.getRepeatNumber());
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                }
            }
        }
        if (! invalidRepeatNumber.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(invalidRepeatNumber);
        }
    }

    private void validateStartEndRanges(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage dateRangeInvalid = new ValidationErrorMessage("Date range is invalid.");
        ValidationErrorMessage timeRangeInvalid = new ValidationErrorMessage("End time is before start time.");

        for (Event event : events) {
            Optional<Date> startDateOpt = parseDateOpt(event.getStartDate());
            Optional<Date> startTimeOpt = parseTimeOpt(event.getStartTime());
            Optional<Date> endDateOpt = parseDateOpt(event.getEndDate());
            Optional<Date> endTimeOpt = parseTimeOpt(event.getEndTime());
            if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
                Date startDate = startDateOpt.get();
                Date endDate = endDateOpt.get();
                if (endDate.before(startDate)) {
                    dateRangeInvalid.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", start date: " + event.getStartDate() + ", end date: " + event.getEndDate() + ". End date is before start date");
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                } else if (startDate.equals(endDate) && startTimeOpt.isPresent() && endTimeOpt.isPresent()) {
                    Date startTime = startTimeOpt.get();
                    Date endTime = endTimeOpt.get();
                    if (endTime.before(startTime)) {
                        timeRangeInvalid.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", start time: " + event.getStartTime() + ", end time: " + event.getEndTime());
                        event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                    }
                }
            }
        }
        if (! dateRangeInvalid.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(dateRangeInvalid);
        }
        if (! timeRangeInvalid.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(timeRangeInvalid);
        }
    }

    private void validateStartDate(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("Start date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.");
        for (Event event : events) {
            Optional<Date> startDateOpt = Optional.empty();
            if (StringUtils.isNotBlank(event.getStartDate())) {
                startDateOpt = parseDateOpt(event.getStartDate());
                if (!startDateOpt.isPresent()) {
                    validationErrorMessage.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", " + event.getStartDate());
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }

    private void validateStartTime(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("Start time is invalid. The time format should be hh:mm. For example, 13:29.");
        for (Event event : events) {
            Optional<Date> startTimeOpt = Optional.empty();
            if (StringUtils.isNotBlank(event.getStartTime())) {
                startTimeOpt = parseTimeOpt(event.getStartTime());
                if (!startTimeOpt.isPresent()) {
                    validationErrorMessage.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", " + event.getStartTime());
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }

    private void validateEndDate(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("End date is invalid or date does not exist. The date format should be dd-mm-yyyy. For example, 12-10-2014.");
        for (Event event : events) {
            Optional<Date> endDateOpt = Optional.empty();
            if (StringUtils.isNotBlank(event.getEndDate())) {
                endDateOpt = parseDateOpt(event.getEndDate());
                if (!endDateOpt.isPresent()) {
                    validationErrorMessage.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", " + event.getEndDate());
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }

    /**
     * Validates that an end-time is present when the start-time is provided and when the start-date is equal to the
     * end-date.
     * @param validationErrorMessageList
     */
    private void validateNonEmptyEndTime(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("Event end-time must be present when an event start-date, start-time and end-date are provided");
        for (Event event : events) {
            if (StringUtils.isNotBlank(event.getStartDate())
                & StringUtils.isNotBlank(event.getStartTime())
                & StringUtils.isNotBlank(event.getEndDate())
                & StringUtils.isBlank(event.getEndTime())) {
                Optional<Date> startDateOpt = parseDateOpt(event.getStartDate());
                Optional<Date> endDateOpt = parseDateOpt(event.getEndDate());
                if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
                    LocalDate startDate = Instant.ofEpochMilli(startDateOpt.get().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = Instant.ofEpochMilli(endDateOpt.get().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                    if (startDate.isEqual(endDate)) {
                        validationErrorMessage.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid());
                        event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                    }
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }

    private void validateEndTime(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("End time is invalid. The time format should be hh:mm. For example, 13:29.");
        for (Event event : events) {
            Optional<Date> endTimeOpt = Optional.empty();
            if (StringUtils.isNotBlank(event.getEndTime())) {
                endTimeOpt = parseTimeOpt(event.getEndTime());
                if (!endTimeOpt.isPresent()) {
                    validationErrorMessage.addOffendingValue("Line number: " + event.getLineNumber() + ", subject: " + event.getSsid() + ", " + event.getEndTime());
                    event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }


    /**
     * Check if any gaps in the event ordinals are present in the input data, compared to the events already present
     * in the study and to the events present in the event data-file.
     * @param validationErrorMessageList
     */
    private void validateEventGap(List<ValidationErrorMessage> validationErrorMessageList) {
        ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage("A gap in event ordinals maybe present when comparing existing events and events present in data file.");
        Map<String, SortedSet<String>> existingEventsForSubject = new HashMap<>();

        for (StudySubjectWithEventsType studySubjectWithEventsType : subjectWithEventsTypeList) {
            String subjectID = studySubjectWithEventsType.getLabel();
            SortedSet<String> subjectExistingEventSet = existingEventsForSubject.get(subjectID);
            if (subjectExistingEventSet == null) {
                subjectExistingEventSet = new TreeSet<>();
                existingEventsForSubject.put(subjectID, subjectExistingEventSet);
            }
            EventsType eventsType = studySubjectWithEventsType.getEvents();
            // add the existing events to the set
            for (EventResponseType eventResponseType : eventsType.getEvent()) {
                subjectExistingEventSet.add(eventResponseType.getOccurrence());
            }
        }
        boolean eventHasError = false;
        for (Event event : events) {
            String subjectID = event.getSsid();
            if (StringUtils.isBlank(subjectID)) {
                validationErrorMessage.addOffendingValue("Unable to determine if an event has a gap because subjectID is missing in line " + event.getLineNumber());
                event.addErrorClassification(ErrorClassification.BLOCK_EVENT);
                eventHasError = true;
            }
            String ordinal = event.getRepeatNumber();
            SortedSet<String> subjectEventSet = existingEventsForSubject.get(subjectID);
            if (subjectEventSet == null) {
                subjectEventSet = new TreeSet<>();
                existingEventsForSubject.put(subjectID, subjectEventSet);
            }
            subjectEventSet.add(ordinal);
        }
        // how check if we have any gaps
        if (! eventHasError) {
            for (String subjectID : existingEventsForSubject.keySet()) {
                SortedSet<String> subjectEventRepeats = existingEventsForSubject.get(subjectID);
                int existingNumberOfRepeats = subjectEventRepeats.size();
                int maxRepeat = 0;
                for (String repeatOrdinal : subjectEventRepeats) {
                    Integer repeatOrdinalInt = null;
                    try {
                        repeatOrdinalInt = Integer.parseInt(repeatOrdinal);
                    } catch (NumberFormatException nfe) {
                        continue;
                    }
                    if (repeatOrdinalInt > maxRepeat) {
                        maxRepeat = repeatOrdinalInt;
                    }
                }
                if ((maxRepeat != subjectEventRepeats.size())) {
                    String addtionalMessage = "";
                    if (existingEventsForSubject != null) {
                        addtionalMessage = ". Subject has " + existingNumberOfRepeats + " event repeats.";
                    }
                    validationErrorMessage.addOffendingValue("Gap found in event ordinals for subject " + subjectID + addtionalMessage);
                    Optional<Subject> subjectWithError =
                            subjects.stream().filter(subject -> subject.getSsid().equalsIgnoreCase(subjectID)).findAny();
                    if (subjectWithError.isPresent()) {
                        subjectWithError.get().addErrorClassification(ErrorClassification.BLOCK_SUBJECT);
                    }
                }
            }
        }
        if (! validationErrorMessage.getOffendingValues().isEmpty()) {
            validationErrorMessageList.add(validationErrorMessage);
        }
    }

    Optional<Date> parseDateOpt(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return Optional.empty();
        }
        ParsePosition pos = new ParsePosition(0);
        Date time = SIMPLE_DATE_FORMAT.parse(dateString, pos);
        //TODO What if empty space left
        if (pos.getIndex() < dateString.length()) {
            return Optional.empty();
        }
        return Optional.ofNullable(time);
    }

    Optional<Date> parseTimeOpt(String timeString) {
        if (StringUtils.isBlank(timeString)) {
            return Optional.empty();
        }
        ParsePosition pos = new ParsePosition(0);
        Date time = SIMPLE_TIME_FORMAT.parse(timeString, pos);
        //TODO What if empty space left
        if (pos.getIndex() < timeString.length()) {
            return Optional.empty();
        }
        return Optional.ofNullable(time);
    }

    Optional<Integer> parseIntOpt(String intString) {
        try {
            return Optional.ofNullable(Integer.valueOf(intString));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
