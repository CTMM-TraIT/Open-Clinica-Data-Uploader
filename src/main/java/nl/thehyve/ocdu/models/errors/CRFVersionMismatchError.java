package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 6/6/16.
 */
public class CRFVersionMismatchError extends ValidationErrorMessage {

    public CRFVersionMismatchError() {
        super("Mismatching Case Report Forms version(s) found. It is not possible to change the CRF version of existing data");
    }

}
