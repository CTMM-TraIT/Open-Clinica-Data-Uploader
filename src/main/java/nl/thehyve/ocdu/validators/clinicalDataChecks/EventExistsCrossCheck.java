package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.EventDoesNotExist;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public class EventExistsCrossCheck implements ClinicalDataCrossCheck {

    private static final Logger log = LoggerFactory.getLogger(EventExistsCrossCheck.class);

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        List<String> validEventNames = new ArrayList<>();
        metaData
                .getEventDefinitions().stream()
                .forEach(eventDefinition -> validEventNames.add(eventDefinition.getName()));
        List<ClinicalData> violators = data.stream()
                .filter(clinicalData -> !validEventNames.contains(clinicalData.getEventName()))
                .collect(Collectors.toList());
        if (violators.size() > 0) {
            ValidationErrorMessage error =
                    new EventDoesNotExist();
            Set<String> nonExistentEventNames = new HashSet<>();
            violators.stream().forEach(clinicalData ->
            { String evName = clinicalData.getEventName();
                nonExistentEventNames.add(evName);
                clinicalData.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
            });
            error.addAllOffendingValues(nonExistentEventNames);
            return error;
        } else return null;
    }
}
