package nl.thehyve.ocdu.models.errors;

/**
 * Created by piotrzakrzewski on 08/06/16.
 */
public class HiddenValueError extends ValidationErrorMessage {
    public HiddenValueError() {
        super("One or more hidden items contain non-empty value(s). Items can be hidden because of a simple conditional display");
    }
}
