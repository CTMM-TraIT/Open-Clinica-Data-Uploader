package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.MessageType;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;

/**
 * Created by jacob on 3/30/17.
 */
public class PersonIdPresentWarning implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput,
                                                        List<String> personIDInSubjectInput) {

        ValidationErrorMessage error = null;
        String personId = subject.getPersonId();

        if (! StringUtils.isEmpty(personId)) {
            error = new ValidationErrorMessage("The subject registration file contains person ids. This will cause problems in your OpenClinica study. Please contact the TraIT Servicedesk!");
            error.setMessageType(MessageType.WARNING);
        }
        return error;
    }
}
