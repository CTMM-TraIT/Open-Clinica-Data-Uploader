package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.SSIDTooLong;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.ErrorFilter;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public class DataFieldWidthCrossCheck implements ClinicalDataCrossCheck {
    public static final int SSID_MAX_LENGTH = 30; //TODO: make configurable

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap){
        Set<String> violatingSSIDs = new HashSet<>();
        data.stream()
                .filter(ocEntity -> ocEntity.getSsid().length() > SSID_MAX_LENGTH)
                .forEach(ocEntity ->    {
                                            violatingSSIDs.add(ocEntity.getSsid());
                                        }
                );
        if (violatingSSIDs.size() > 0) {
            ValidationErrorMessage error = new SSIDTooLong(SSID_MAX_LENGTH);
            error.addAllOffendingValues(violatingSSIDs);
            ErrorFilter errorFilter = new ErrorFilter(data);
            errorFilter.addErrorToSubjects(violatingSSIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;
    }
}
