package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 8/3/16.
 */
public class EventRepeatFormatError extends ValidationErrorMessage {

    public EventRepeatFormatError() {
        super("A invalid value found in an event repeat");
    }
}
