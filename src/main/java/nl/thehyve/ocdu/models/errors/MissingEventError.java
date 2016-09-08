package nl.thehyve.ocdu.models.errors;

/**
 * Error to indicate that an data is present in the data file for an event which is not present in OpenClinica nor
 * present in the event data.
 * Created by jacob on 8/10/16.
 */
public class MissingEventError extends ValidationErrorMessage {

    public MissingEventError() {
        super("One or more events are present in the data which are not scheduled in OpenClinica and which are not present in the event-file. "
                + "If sites are specified for new subjects in the subject-file, then the same site must also be present in the event-file. "
                + "OpenClinica will report incorrectly that data has been uploaded for these events");
    }
}
