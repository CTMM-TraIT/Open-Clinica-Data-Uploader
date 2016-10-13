package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Checks if there are duplicate subject ID in the input data.
 * Created by jacob on 10/13/16.
 */
public class DuplicateSubjectLabelDataCheck implements PatientDataCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput) {
        ValidationErrorMessage error = null;

        String subjectLabel = subject.getSsid();
        int frequency = Collections.frequency(subjectIDInSubjectInput, subjectLabel);
        if (frequency > 1) {
            error = new ValidationErrorMessage("Duplicate subject ID found in data");
            error.setError(true);
        }

        if (error != null) {
            String commonMessage = getCommonErrorMessage(index, subjectLabel);
            error.addOffendingValue(commonMessage);
            return error;
        }
        return null;
    }
}
