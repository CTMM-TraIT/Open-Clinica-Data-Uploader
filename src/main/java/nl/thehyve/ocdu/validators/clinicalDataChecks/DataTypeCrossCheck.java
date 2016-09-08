package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.DataTypeMismatch;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 12/05/16.
 */
public class DataTypeCrossCheck implements ClinicalDataCrossCheck {
    public final static String TEXT_DATA_TYPE = "text";
    public final static String INTEGER_DATA_TYPE = "integer";
    public final static String FLOAT_DATA_TYPE = "float";
    public final static String DATE_DATA_TYPE = "date";
    public final static String PARTIAL_DATE_DATA_TYPE = "partialDate";



    private static Map<String, String> humanReadbleTypes = initHumanReadbleTypes();

    private static Map<String, String> initHumanReadbleTypes() {
        Map<String, String> humanReadble = new HashMap<>();
        humanReadble.put(TEXT_DATA_TYPE, "text");
        humanReadble.put(INTEGER_DATA_TYPE, "integer number (e.g. 2)");
        humanReadble.put(FLOAT_DATA_TYPE, "real number (e.g. 12.3)");
        humanReadble.put(PARTIAL_DATE_DATA_TYPE, "partial date (e.g: 1996)");
        humanReadble.put(DATE_DATA_TYPE, "full date (e.g: 16-05-1988) date must also be a valid " +
                "gregorian calendar entry (e.g. 31-02-2001 is not)");
        return humanReadble;
    }

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        Map<ClinicalData, String> itemDataTypes = buildDataTypeMap(data, itemDefMap);
        Set<String> offenders = new HashSet<>();
        for (ClinicalData clinicalData : data) {
            ItemDefinition itemDefinition = itemDefMap.get(clinicalData);
            String expectedType = itemDataTypes.get(clinicalData);
            if ((itemDefinition != null) && (StringUtils.isNotBlank(expectedType))) {
                List<String> values = clinicalData.getValues(itemDefinition.isMultiselect());
                if (! allValuesMatch(values, expectedType)) {
                    offenders.add(clinicalData.toOffenderString() + " Expected: " + humanReadbleTypes.get(itemDataTypes.get(clinicalData)));
                }
            }
        }

/*        Set<String> offenders = data.stream()
                .filter(clinicalData -> !allValuesMatch(clinicalData.getValues(itemDefMap.get(clinicalData).isMultiselect()), itemDataTypes.get(clinicalData)) && shownMap.get(clinicalData))
                .map(clinicalData -> clinicalData.toOffenderString() + " Expected: " + humanReadbleTypes.get(itemDataTypes.get(clinicalData)))
                .collect(Collectors.toSet());*/
        if (offenders.size() > 0) {
            DataTypeMismatch error = new DataTypeMismatch();
            offenders.stream().
                    forEach(offender -> {
                        error.addOffendingValue(offender);
                    });
            return error;
        } else return null;
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

    private boolean allValuesMatch(List<String> values, String expectedType) {
        for (String value : values) {
            if (!matchType(value, expectedType)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchType(String value, String expectedType) {
        if (expectedType == null || expectedType.equals(TEXT_DATA_TYPE)) {
            return true;
        } else if (expectedType.equals(INTEGER_DATA_TYPE)) {
            return UtilChecks.isInteger(value);
        } else if (expectedType.equals(FLOAT_DATA_TYPE)) {
            return UtilChecks.isFloat(value);
        } else if (expectedType.equals(DATE_DATA_TYPE)) {
            return UtilChecks.isDate(value);
        } else if (expectedType.equals(PARTIAL_DATE_DATA_TYPE)) {
            return UtilChecks.isPDate(value);
        } else {
            return true; // no expectations, no disappointment
        }
    }

}
