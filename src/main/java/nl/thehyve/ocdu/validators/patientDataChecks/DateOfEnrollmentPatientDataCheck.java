package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by bo on 6/15/16.
 */
public class DateOfEnrollmentPatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String dateOfEntrollment = subject.getDateOfEnrollment();
        Date currentDate = new Date();

        if (StringUtils.isBlank(dateOfEntrollment)) {
            error = new ValidationErrorMessage("Date of Enrollment is not provided. Today's date will be used. ");
            subject.setDateOfEnrollment(currentDate.toString());
            error.setError(false);
        } else {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            dateFormat.setLenient(false);
            if (!UtilChecks.isDate(dateOfEntrollment)) {
                error = new ValidationErrorMessage("Enrollment date format is invalid. The date format should be dd-mm-yyyy. For example, 23-10-2012.");
            } else  {
                try {
                    Date date = dateFormat.parse(dateOfEntrollment);
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
