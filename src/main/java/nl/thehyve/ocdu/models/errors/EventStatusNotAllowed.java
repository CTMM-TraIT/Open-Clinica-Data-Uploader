package nl.thehyve.ocdu.models.errors;

import javax.validation.Valid;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class EventStatusNotAllowed extends ValidationErrorMessage {
    public EventStatusNotAllowed() {
        super("One or more subjects have event-statuses or have CRF's which do not allow uploading data. Subject(s) will not be uploaded");
    }
}
