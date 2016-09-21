package nl.thehyve.ocdu.validators.autonomous;

import nl.thehyve.ocdu.models.errors.MissingMappingFileError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.services.MappingService;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by jacob on 9/19/16.
 */
public class MissingMappingFileValidator implements AutonomousValidator {


    public ValidationErrorMessage getCorrespondingError(Path baseDirectory) {
        File[] directoryList = baseDirectory.toFile().listFiles(File::isDirectory);
        MissingMappingFileError ret = new MissingMappingFileError();
        for (File file : directoryList) {
            File pathToMappingFile = file.toPath().resolve(MappingService.MAPPING_FILE_NAME).toFile();
            if (! pathToMappingFile.exists()) {
                ret.addOffendingValue(pathToMappingFile.getParent().toString());
            }
        }
        return ret;
    }
}
