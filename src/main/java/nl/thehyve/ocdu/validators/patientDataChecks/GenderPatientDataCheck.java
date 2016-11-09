package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;

/**
 * Created by bo on 6/7/16.
 */
public class GenderPatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput, List<ClinicalData> clinicalDataList) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        boolean isGenderRequired = metaData.isGenderRequired();
        if (!isGenderRequired) {
            for (SiteDefinition sd : metaData.getSiteDefinitions()) {
                if (sd.isGenderRequired()) {
                    isGenderRequired = sd.isGenderRequired();
                    break;
                }
            }
        }


        ValidationErrorMessage error = null;
        if (!isGenderRequired && !StringUtils.isBlank(subject.getGender())) {
            error = new ValidationErrorMessage("It is not allowed to upload gender by the study protocol");
        } else if (isGenderRequired) {
            if(StringUtils.isEmpty(subject.getGender()) ) {
                error = new ValidationErrorMessage("Gender needs to be specified as m or f. ");
            }
            else if(!subject.getGender().equals("m") && !subject.getGender().equals("f")) {
                error = new ValidationErrorMessage("Gender needs to be specified as m or f. ");
            }
        }

        if (error != null) {
            UtilChecks.addErrorClassificationToSingleSubject(clinicalDataList, ssid, ErrorClassification.BLOCK_SUBJECT);
            error.addOffendingValue(commonMessage + " gender: " + subject.getGender());
        }

        return error;
    }
}
