package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.EventRepeatFormatError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks if the event repeat is a valid integer.
 * Created by jacob on 8/3/16.
 */
public class EventRepeatFormatCheck implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        EventRepeatFormatError error = new EventRepeatFormatError();
        Map<String, Set<String>> repeatMap = data.stream()
                .collect(Collectors.groupingBy(ClinicalData::getSsid,
                        Collectors.mapping(ClinicalData::getEventRepeat, Collectors.toSet())));

        for (Map.Entry<String, Set<String>> entry : repeatMap.entrySet()) {
            Set<String> repeatsPerSubject = entry.getValue();
            for (String repeatValue : repeatsPerSubject) {
                try {
                    Integer.parseInt(repeatValue);
                } catch (NumberFormatException nfe) {
                    error.addOffendingValue(entry.getKey());
                }
            }
        }
        if (error.getOffendingValues().size() > 0) {
            return error;
        }
        return null;
    }
}
