package nl.thehyve.ocdu.models.errors;

import nl.thehyve.ocdu.services.ValidationService;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public class EventDoesNotExist extends ValidationErrorMessage {
    public EventDoesNotExist() {
        super("The events used in your data file is not defined in OpenClinica. Please correct");
    }
}
