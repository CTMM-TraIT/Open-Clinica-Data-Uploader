package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;

/**
 * Created by piotrzakrzewski on 18/07/16.
 */
public class PresentInData implements PatientDataCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData, List<StudySubjectWithEventsType> subjectWithEventsTypes, List<String> ssidsInData) {
        if (!ssidsInData.contains(subject.getSsid())) {
            ValidationErrorMessage error = new ValidationErrorMessage("One or more subjects are absent in the data file. Please use generated template.");
            error.addOffendingValue(getCommonErrorMessage(index, subject.getSsid()));
            return error;
        }
        return null;
    }
}
