package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.RepeatInNonrepeatingEvent;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 17/05/16.
 */
public class EventRepeatCrossCheck implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        // if  there is a non repeating event which has repeat higher than 1 return error
        RepeatInNonrepeatingEvent error = new RepeatInNonrepeatingEvent();

        List<String> repeatList =
                data.stream().map(ClinicalData::getEventRepeat).collect(Collectors.toList());
        boolean containsInvalidInt = false;
        try {
            for (String intValue : repeatList) {
                int value = Integer.parseInt(intValue);
            }
        }
        catch (NumberFormatException nfe) {
            containsInvalidInt = true;
        }
        if (containsInvalidInt) {
            return null;
        }

        Set<String> offenders = data.stream().filter(clinicalData -> Integer.parseInt(clinicalData.getEventRepeat()) > 1)
                .filter(clinicalData -> isViolator(clinicalData, metaData))
                .map(clinicalData -> "Event " + clinicalData.getEventName() + " repeat: " + clinicalData.getEventRepeat())
                .collect(Collectors.toSet());

        Set<String> offenderSubjectIDs = data.stream().filter(clinicalData -> Integer.parseInt(clinicalData.getEventRepeat()) > 1)
                .filter(clinicalData -> isViolator(clinicalData, metaData))
                .map(clinicalData -> clinicalData.getSsid())
                .collect(Collectors.toSet());

        offenders.forEach(offender -> error.addOffendingValue(offender));

        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else
            return null;
    }

    private boolean isViolator(ClinicalData clinicalData, MetaData metaData) {
        return !isRepeating(clinicalData.getEventName(), metaData);
    }

    private boolean isRepeating(String eventName, MetaData metaData) {
        return metaData.getEventDefinitions().stream()
                .anyMatch(eventDefinition -> eventDefinition.getName().equals(eventName)
                        && eventDefinition.isRepeating());
    }

}
