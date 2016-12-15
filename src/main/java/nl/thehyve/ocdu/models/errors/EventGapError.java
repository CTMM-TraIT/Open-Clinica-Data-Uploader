package nl.thehyve.ocdu.models.errors;

/**
 * Error which indicates that a gap is present in the event repeats.
 * Created by jacob on 8/1/16.
 */
public class EventGapError extends ValidationErrorMessage {

    public EventGapError() {
        super("The Event registration file has gaps between Event occurrences, possibly compared to Events already present in OpenClinica. Gaps are not allowed. Please correct.");
    }
}
