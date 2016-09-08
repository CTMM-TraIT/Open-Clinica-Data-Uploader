package nl.thehyve.ocdu.validators;


import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.patientDataChecks.*;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by bo on 6/7/16.
 */
public class PatientDataOcChecks {

    private final List<Subject> subjects;
    private final MetaData metadata;
    private final List<StudySubjectWithEventsType> subjEventData;
    private final Set<String> ssidsInData;

    private List<PatientDataCheck> checks = new ArrayList<>();

    public PatientDataOcChecks(MetaData metadata, List<Subject> subjects, List<StudySubjectWithEventsType> subjectWithEventsTypes,
                               Set<String> ssidsInData) {
        this.metadata = metadata;
        this.subjects = subjects;
        this.subjEventData = subjectWithEventsTypes;
        this.ssidsInData = ssidsInData;
        checks.add(new GenderPatientDataCheck());
        checks.add(new DateOfBirthPatientDataCheck());
        checks.add(new PersonIdPatientDataCheck());
        checks.add(new DuplicatePersonIdDataCheck());
        checks.add(new DateOfEnrollmentPatientDataCheck());
        checks.add(new SecondaryIdPatientDataCheck());
        checks.add(new StudyPatientDataCheck());
        checks.add(new SitePatientDataCheck());
        checks.add(new SubjectNotRegistered());
        checks.add(new PresentInData());
        checks.add(new MissingSiteWarningCheck());
    }

    public List<ValidationErrorMessage> getErrors() {
        List<ValidationErrorMessage> combinedErrors = new ArrayList<>();
        for (PatientDataCheck check : checks) {
            List<ValidationErrorMessage> errors = new ArrayList<>();
            int index = 1;
            for (Subject subject : subjects) {
                ValidationErrorMessage error = check.getCorrespondingError(index, subject, metadata, subjEventData,
                        ssidsInData);
                if (error != null) {
                    error.setSubject(subject.getSsid());
                    errors.add(error);
                }
                index++;
            }
            if (! errors.isEmpty()) {
                String cause = errors.get(0).getMessage();
                boolean isError = errors.get(0).isError();
                ValidationErrorMessage combinedErrorMessage = new ValidationErrorMessage(cause);
                combinedErrorMessage.setError(isError);
                for (ValidationErrorMessage validationErrorMessage : errors) {
                    combinedErrorMessage.addAllOffendingValues(validationErrorMessage.getOffendingValues());
                }
                combinedErrors.add(combinedErrorMessage);
            }
        }
        return combinedErrors;
    }
}
