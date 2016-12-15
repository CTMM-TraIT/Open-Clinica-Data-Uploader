package nl.thehyve.ocdu.models.errors;

import javax.validation.Valid;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class EventStatusNotAllowed extends ValidationErrorMessage {
    public EventStatusNotAllowed() {
        super("Subject- or event-status does not allow for uploading data. Subject or event will be removed");
    }
}
