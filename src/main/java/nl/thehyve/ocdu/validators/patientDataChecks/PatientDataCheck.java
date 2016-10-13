package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;

/**
 * Created by bo on 6/7/16.
 */
public interface PatientDataCheck {
    ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                 List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                 List<String> ssidsInData);

    default String getCommonErrorMessage(int index, String ssid) {
        return "Line " + index + " (subjectID = " + ssid + ") : ";
    }
}
