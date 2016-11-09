package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;

/**
 * Created by bo on 6/15/16.
 */
public class StudyPatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput, List<ClinicalData> clinicalDataList) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String study = subject.getStudy();
        String mStudy = metaData.getProtocolName();

        if (StringUtils.isBlank(study)) {
            error = new ValidationErrorMessage("Study should be provided.");
            error.addOffendingValue(commonMessage + " study: " + subject.getStudy());
        }
        else if(!study.equals(mStudy)) {
            error = new ValidationErrorMessage("Study provided in the template is inconsistent with the study defined in the data file.");
            error.addOffendingValue(commonMessage + " study in template: " + study + ", study in data file: " + mStudy);
        }
        if (error != null) {
            UtilChecks.addErrorClassificationToSingleSubject(clinicalDataList, ssid, ErrorClassification.BLOCK_SUBJECT);
        }
        return error;
    }
}
