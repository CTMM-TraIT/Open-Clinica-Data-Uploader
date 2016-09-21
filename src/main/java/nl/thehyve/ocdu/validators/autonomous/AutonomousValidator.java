package nl.thehyve.ocdu.validators.autonomous;

import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;

import java.nio.file.Path;

/**
 * Created by jacob on 9/19/16.
 */
public interface AutonomousValidator {

    /**
     * We include the mapping.yml file to create the {@link nl.thehyve.ocdu.models.UploadSession}.
     */
    public static final String[] DATA_FILE_EXTENSIONS_USED = {"txt", "dat", "tsv"};

    public static final String DATA_FILE_SUFFIX = "-data";
    public static final String EVENT_FILE_SUFFIX = "-events";
    public static final String SUBJECT_FILE_SUFFIX = "-subjects";

    public ValidationErrorMessage getCorrespondingError(Path baseDirectory);
}
