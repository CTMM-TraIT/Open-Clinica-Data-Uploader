package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.EventGapError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.ErrorFilter;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks if an upload will create a gap in the ordinal of repeating events
 * Created by jacob on 8/1/16.
 */
public class EventGapCrossCheck implements ClinicalDataCrossCheck {

    private static final Logger log = LoggerFactory.getLogger(EventGapCrossCheck.class);

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        EventGapError error = new EventGapError();
        // create a map of each subject with the repeats in the data
        Map<String, Set<String>> newSubjectRepeats = data.stream()
                                        .filter(clinicalData -> isRepeating(clinicalData.getEventName(), metaData))
                                        .collect(Collectors.groupingBy(ClinicalData::createEventRepeatKey,
                                                Collectors.mapping(ClinicalData::getEventRepeat, Collectors.toSet())));

        Map<String, Set<String>> existingSubjectRepeats = new HashMap<>();
        for (StudySubjectWithEventsType studySubjectWithEventsType : studySubjectWithEventsTypeList) {
            List<ClinicalData> existingClinicalDataListForSubject = createFromStudySubjectWithEventsType(studySubjectWithEventsType, metaData);
            Map<String, Set<String>> subjectRepeats = existingClinicalDataListForSubject.stream()
                    .collect(Collectors.groupingBy(ClinicalData::createEventRepeatKey,
                            Collectors.mapping(ClinicalData::getEventRepeat, Collectors.toSet())));
            existingSubjectRepeats.putAll(subjectRepeats);
        }

        Map<String, Set<String>> combinedSubjectRepeats = new HashMap<>();
        combinedSubjectRepeats.putAll(existingSubjectRepeats);
        for (String subjectID : newSubjectRepeats.keySet()) {
            if (combinedSubjectRepeats.containsKey(subjectID)) {
                Set<String> newRepeatSet = newSubjectRepeats.get(subjectID);
                combinedSubjectRepeats.get(subjectID).addAll(newRepeatSet);
            }
            else {
                combinedSubjectRepeats.put(subjectID, newSubjectRepeats.get(subjectID));
            }
        }
        Set<String> subjectIDSetWithError = new HashSet<>();
        for (Map.Entry<String, Set<String>> repeatSetEntryList : combinedSubjectRepeats.entrySet()) {
            Set<String> repeatSet = repeatSetEntryList.getValue();
            List<String> repeatList = new ArrayList<>(repeatSet);
            Collections.sort(repeatList);
            Optional<String> maxOptional = repeatList.stream().max(Comparator.naturalOrder());
            if (maxOptional.isPresent()) {
                int max = Integer.parseInt(maxOptional.get());
                if (max - repeatList.size() != 0) {
                    String key = repeatSetEntryList.getKey();
                    String[] keyPartList = StringUtils.splitPreserveAllTokens(key, ClinicalData.KEY_SEPARATOR);
                    error.addOffendingValue("Subject: " + keyPartList[2] + ", Event: " + keyPartList[3]);
                    subjectIDSetWithError.add(keyPartList[2]);
                }
            }
        }
        if (error.getOffendingValues().size() > 0) {
            ErrorFilter errorFilter = new ErrorFilter(data);
            errorFilter.addErrorToSubjects(subjectIDSetWithError, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        }
        return null;
    }

    private List<ClinicalData> createFromStudySubjectWithEventsType(StudySubjectWithEventsType studySubjectWithEventsType, MetaData metaData) {
        List<ClinicalData> ret = new ArrayList<>();
        for (EventResponseType eventResponseType : studySubjectWithEventsType.getEvents().getEvent()) {
            String eventName = eventResponseType.getEventDefinitionOID();
            Optional<EventDefinition> matchingEventDefinition =
                    (metaData.getEventDefinitions().stream().filter(eventDefinition -> eventDefinition.getStudyEventOID().equals(eventName)).findFirst());

            if (matchingEventDefinition.isPresent() &&
                    matchingEventDefinition.get().isRepeating()) {
                ClinicalData clinicalData = new ClinicalData();
                clinicalData.setStudy(studySubjectWithEventsType.getStudyRef().getIdentifier());
                if (studySubjectWithEventsType.getStudyRef().getSiteRef() != null) {
                    clinicalData.setSite(studySubjectWithEventsType.getStudyRef().getSiteRef().getIdentifier());
                }
                else {
                    clinicalData.setSite("");
                }
                clinicalData.setSsid(studySubjectWithEventsType.getLabel());
                clinicalData.setEventName(metaData.findEventName(eventResponseType.getEventDefinitionOID()));
                clinicalData.setEventRepeat(eventResponseType.getOccurrence());
                ret.add(clinicalData);
            }
        }
        return ret;
    }


    private boolean isRepeating(String eventName, MetaData metaData) {
        return metaData.getEventDefinitions().stream()
                .anyMatch(eventDefinition -> eventDefinition.getName().equals(eventName)
                        && eventDefinition.isRepeating());
    }
}
