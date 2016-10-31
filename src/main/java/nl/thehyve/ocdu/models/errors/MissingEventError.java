package nl.thehyve.ocdu.models.errors;

/**
 * Error to indicate that an data is present in the data file for an event which is not present in OpenClinica nor
 * present in the event data.
 * Created by jacob on 8/10/16.
 */
public class MissingEventError extends ValidationErrorMessage {

    public MissingEventError() {
        super("The (repeated) events indicated in the event registration file do not match the events present " +
                "in the data file. To upload the data you must upload a new event-file (via back-button). If you "+
                "click 'next' data for the unscheduled Events will not be uploaded");
    }
}
