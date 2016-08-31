package nl.thehyve.ocdu.services;

/**
 * Signals a problem in the input
 * Created by jacob on 8/31/16.
 */
public class InputValidationException extends Exception {

    public InputValidationException(String message) {
        super(message);
    }
}
