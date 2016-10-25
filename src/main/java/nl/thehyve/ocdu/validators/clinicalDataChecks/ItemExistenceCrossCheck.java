package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.errors.ItemDoesNotExist;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 11/05/16.
 */
public class ItemExistenceCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<String, Set<String>> allItemNames = metaData.obtainFormOIDItemNameMap();
        Set<String> missing = data.stream().filter(clinicalData -> {
            String item = clinicalData.getItem();
            String formOID = metaData.findFormOID(clinicalData.getCrfName(), clinicalData.getCrfVersion());
            Set<String> itemPresentInForm = allItemNames.get(formOID);
            if (itemPresentInForm == null) {
                return false;
            }
            return !(itemPresentInForm.contains(item));
        }).map(clinicalData -> clinicalData.getItem()).collect(Collectors.toSet());

        if (missing.size() > 0) {
            ItemDoesNotExist error = new ItemDoesNotExist();
            missing.forEach(itemName -> {
                if (itemName.equals("")) {
                    error.addOffendingValue(" (Empty string)");
                } else {
                    error.addOffendingValue(itemName);
                }
            });
            return error;
        } else {
            return null;
        }
    }
}
