package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.CodeListDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.EnumerationError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 17/05/16.
 */
public class CodeListCrossCheck implements ClinicalDataCrossCheck {


    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<ClinicalData, String> itemDataTypes = buildDataTypeMap(data, itemDefMap);
        Map<String, Set<String>> allItemNames = metaData.obtainFormOIDItemNameMap();
        EnumerationError error = new EnumerationError();
        Map<String, CodeListDefinition> codeListMap = new HashMap<>();
        metaData.getCodeListDefinitions().stream().forEach(codeListDefinition -> {
            codeListMap.put(codeListDefinition.getOcid(), codeListDefinition);
        });
        data.stream().forEach(clinicalData -> {
            String formOID = metaData.findFormOID(clinicalData.getCrfName(), clinicalData.getCrfVersion());
            if (StringUtils.isNotEmpty(formOID)) {
                ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
                Set<String> itemNamesPresentInForm = allItemNames.get(formOID);
                if ((itemDefinition != null) && (itemNamesPresentInForm.contains(clinicalData.getItem()))) {
                    List<String> values = clinicalData.getValues(itemDefinition.isMultiselect());
                    if (shownMap.get(clinicalData)) { // Non existent item is a separate error, hidden values are not validated
                        String codeListRef = itemDefinition.getCodeListRef();
                        if (codeListRef != null) {
                            CodeListDefinition codeListdef = codeListMap.get(codeListRef);
                            String expectedType = itemDataTypes.get(clinicalData);
                            boolean isNotEmpty = StringUtils.isNotEmpty(clinicalData.getValue());
                            for (String value : values) {
                                if ((codeListdef != null) && (!codeListdef.isAllowed(value, expectedType)) && isNotEmpty) {
                                    String msg = clinicalData.toOffenderString() + " value not in: " + codeListdef;
                                    if (value.contains(" ")) msg += " (value contains whitespaces)";
                                    else if (value.equals("")) msg += " (value is an empty string)";
                                    error.addOffendingValue(msg);
                                }
                            }
                        }
                    }
                }
            }
        });
        if (error.getOffendingValues().size() > 0) {
            return error;
        } else
            return null;
    }

    private Map<ClinicalData, String> buildDataTypeMap(List<ClinicalData> data, Map<ClinicalData, ItemDefinition> defMap) {
        Map<ClinicalData, String> typeMap = new HashMap<>();
        for (ClinicalData clinicalData : data) {
            ItemDefinition def = defMap.get(clinicalData);
            if (def != null) {
                typeMap.put(clinicalData, def.getDataType());
            }
        }
        return typeMap;
    }
}
