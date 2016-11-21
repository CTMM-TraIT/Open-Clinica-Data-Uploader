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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bo on 6/15/16.
 */
public class DateOfBirthPatientDataCheck implements PatientDataCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput) {

        int DOBrequired = metaData.getBirthdateRequired();
        for (int i = 0; i < metaData.getSiteDefinitions().size(); i++) {
            SiteDefinition sd = metaData.getSiteDefinitions().get(i);
            int site_dob = sd.getBirthdateRequired();
            //if site requirement for dateOfBirth is more specific than that of study,
            //update DOBrequired
            if (site_dob < DOBrequired) {
                DOBrequired = site_dob;
            }
        }

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String dob = subject.getDateOfBirth();
        if (!StringUtils.isBlank(dob) && DOBrequired == MetaData.BIRTH_DATE_NOT_USED) { // 3 means not required
            error = new ValidationErrorMessage("Date of birth submission is not allowed by the study protocol");
            error.addOffendingValue(commonMessage + " Date of birth: " + subject.getDateOfBirth());
            subject.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
        } else if (!StringUtils.isBlank(dob) || DOBrequired < MetaData.BIRTH_DATE_NOT_USED) {
            String label = " ";
            String msg = null;
            if (DOBrequired == MetaData.BIRTH_DATE_AS_FULL_DATE) {//FULL DATE
                msg = checkFullDate(dob);
                label = " Date of birth: ";
            } else if (DOBrequired == MetaData.BIRTH_DATE_AS_ONLY_YEAR) { // YEAR ONLY
                msg = checkYearOnly(dob);
                label = " Year of birth: ";
            }
            if (msg != null) {
                error = new ValidationErrorMessage(commonMessage + msg);
                error.addOffendingValue(commonMessage + label + subject.getDateOfBirth());
                subject.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
            }
        }

        return error;
    }

    private String checkYearOnly(String dob) {
        try {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int birthYear = Integer.valueOf(dob);
            if (birthYear > currentYear) {
                return "Birth year can not be greater than current year";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Birth year format is invalid. The year should be four digits, for example, 1998.";
        }
    }

    private String checkFullDate(String dob) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        if (StringUtils.isEmpty(dob)) {
            return "Date of birth is missing.";
        }
        try {
            Date date = dateFormat.parse(dob);
            Date currentDate = new Date();
            if (currentDate.before(date)) {
                return "Birth date should be in the past.";
            }
            return null;
        } catch (ParseException e) {
            return "Birth date format is invalid or the date does not exist. The date format should be dd-mm-yyyy. For example, 23-10-2012.";
        }
    }
}
