package nl.thehyve.ocdu.models.errors;

/**
 * Warning for the user that the data-file contains data which is already present in a OpenClinica CRF and which
 * has the status <code>data entry started</code> or <code>data entry completed</code>.
 * Created by jacob on 9/13/16.
 */
public class EventStatusWarningForOverwrite extends ValidationErrorMessage {

    public EventStatusWarningForOverwrite() {
        super("The CRF for which you are about to upload data has the status 'Data Entry Started' or 'Data Entry Completed' for one or more subjects");
    }
}
