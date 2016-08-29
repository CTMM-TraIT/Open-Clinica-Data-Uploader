package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.StudyStatusError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class StudyStatusAvailable implements ClinicalDataCrossCheck {

    public static final String STUDY_STATUS_ALLOWING_UPLOAD = "available";

    /**
     * Map between the technical study status returned by the Metadata web-service call and the value is is displayed to
     * the users.
     */
    private static final Map<String, String> OPENCLINCA_STATUS_MAP = new HashMap<>();
    static {
        OPENCLINCA_STATUS_MAP.put("pending", "Design");
        OPENCLINCA_STATUS_MAP.put(STUDY_STATUS_ALLOWING_UPLOAD, "Available");
        OPENCLINCA_STATUS_MAP.put("frozen", "Frozen");
        OPENCLINCA_STATUS_MAP.put("locked", "Locked");
    }

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        if (! STUDY_STATUS_ALLOWING_UPLOAD.equals(metaData.getStatus())) {
            ValidationErrorMessage error = new StudyStatusError();
            error.addOffendingValue("Study: " + metaData.getStudyName() + " has status: " +  OPENCLINCA_STATUS_MAP.get(metaData.getStatus()));
            return error;
        }
        return null;
    }
}
