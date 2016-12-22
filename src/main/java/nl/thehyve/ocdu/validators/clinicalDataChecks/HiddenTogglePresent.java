package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.DisplayRule;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ToggleVarForDisplayRuleAbsent;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 15/06/16.
 */
public class HiddenTogglePresent implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        ToggleVarForDisplayRuleAbsent error = new ToggleVarForDisplayRuleAbsent();
        Set<String> errors = new HashSet<>();
        Set<String> offenderSubjectIDs = new HashSet<>();
        for (ClinicalData clinicalData : itemDefMap.keySet()) {
            List<DisplayRule> displayRules = itemDefMap.get(clinicalData).getDisplayRules();
            for (DisplayRule displayRule : displayRules) {
                boolean exists = itemExists(data, displayRule.getControlItemName());
                if (!exists) {
                    error.addOffendingValue(clinicalData.toOffenderString() + " requires: " + displayRule.getControlItemName());
                    offenderSubjectIDs.add(clinicalData.getSsid());
                }
            }
        }
        error.addAllOffendingValues(errors);
        if (error.getOffendingValues().size() > 0) {
            UtilChecks.addErrorClassificationForSubjects(data, offenderSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;
    }

    private boolean itemExists(List<ClinicalData> data, String itemName) {
        return data.stream().anyMatch(clinicalData -> clinicalData.getItem().equals(itemName));
    }
}
