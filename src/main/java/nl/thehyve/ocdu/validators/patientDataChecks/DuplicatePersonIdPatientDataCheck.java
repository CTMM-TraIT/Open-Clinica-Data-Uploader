package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by jacob on 23-11-2016
 */
public class DuplicatePersonIdPatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput,
                                                        List<String> personIDInSubjectInput) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String personId = subject.getPersonId();

        if (! (metaData.getPersonIDUsage() == ProtocolFieldRequirementSetting.BANNED) && (! StringUtils.isEmpty(personId))) {
            int frequency = Collections.frequency(personIDInSubjectInput, personId);
            if (frequency > 1) {
                error = new ValidationErrorMessage("Duplicate person ID present in subject data");
            }
        }

        if(error != null) {
            error.addOffendingValue(commonMessage + " person ID: " + subject.getPersonId());
        }
        return error;
    }

}
