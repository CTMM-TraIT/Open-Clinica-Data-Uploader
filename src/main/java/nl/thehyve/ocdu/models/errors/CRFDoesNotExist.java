package nl.thehyve.ocdu.models.errors;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public class CRFDoesNotExist extends ValidationErrorMessage {
    public CRFDoesNotExist() {
        super("The CRF used in your data file is not present in the referenced event. Please correct.");
    }
}
