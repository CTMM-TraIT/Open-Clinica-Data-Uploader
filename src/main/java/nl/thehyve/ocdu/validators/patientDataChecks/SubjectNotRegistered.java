package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;

/**
 * Created by piotrzakrzewski on 06/07/16.
 */
public class SubjectNotRegistered implements PatientDataCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput,
                                                        List<String> personIDInSubjectInput) {
        String ssid = subject.getSsid();
        for (StudySubjectWithEventsType subjectInfo : subjectWithEventsTypes) {
            if (subjectInfo.getLabel().equals(ssid)) {
                return getError(index, subject, metaData);
            }
        }
        return null;
    }

    private ValidationErrorMessage getError(int index, Subject subject, MetaData metaData) {
        String errorMsg = getCommonErrorMessage(index, subject.getSsid());
        ValidationErrorMessage error = new ValidationErrorMessage(" One or more subjects are already registered in study: "
                + metaData.getStudyName());
        error.addOffendingValue(errorMsg);
        return error;
    }
}
