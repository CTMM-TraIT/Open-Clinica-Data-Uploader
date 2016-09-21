package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 9/21/16.
 */
public class MissingMappingFileError extends ValidationErrorMessage {

    public MissingMappingFileError() {
        super("One or more apping file(s) abstent in the following directories");
    }
}
