package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by bo on 6/15/16.
 */
public class DateOfEnrollmentPatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        List<String> ssidsInData) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String dateOfEnrollment = subject.getDateOfEnrollment();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        Date currentDate = new Date();
        if (StringUtils.isBlank(dateOfEnrollment)) {
            error = new ValidationErrorMessage("Date of Enrollment is not provided. Today's date will be used. ");
            dateOfEnrollment = dateFormat.format(new Date());
            subject.setDateOfEnrollment(dateOfEnrollment);
            error.setError(false);
        } else {

            if (!UtilChecks.isDate(dateOfEnrollment)) {
                error = new ValidationErrorMessage("Enrollment date format is invalid. The date format should be dd-mm-yyyy. For example, 23-10-2012.");
            } else  {
                try {
                    Date date = dateFormat.parse(dateOfEnrollment);
                    if ((currentDate.before(date))) {
                        error = new ValidationErrorMessage("Date of Enrollment should be in the past.");
                    }
                } catch (Exception e) {
                    error = new ValidationErrorMessage("Enrollment date format is invalid. The date format should be dd-mm-yyyy. For example, 23-10-2012.");
                    e.printStackTrace();
                }
            }
        }

        if(error != null) {
            error.addOffendingValue(commonMessage + " date of enrollment: " + subject.getDateOfEnrollment());
        }

        return error;
    }

}
