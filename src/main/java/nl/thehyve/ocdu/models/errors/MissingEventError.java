package nl.thehyve.ocdu.models.errors;

/**
 * Error to indicate that an data is present in the data file for an event which is not present in OpenClinica nor
 * present in the event data.
 * Created by jacob on 8/10/16.
 */
public class MissingEventError extends ValidationErrorMessage {

    public MissingEventError() {
        super("Event in the event scheduling file does not match the event in the data file. Please click on back, " +
                "correct the event file and upload again. If you do proceed, " +
                "the data for unscheduled events will not be uploaded");
    }
}
