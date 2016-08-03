package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 8/3/16.
 */
public class MissingPersonIDError extends ValidationErrorMessage {

    public MissingPersonIDError() {
        super("One or more subjects have a missing mandatory person ID");
    }
}
