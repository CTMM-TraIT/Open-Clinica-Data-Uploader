package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.autonomous.AutonomousValidator;
import nl.thehyve.ocdu.validators.autonomous.MissingMappingFileValidator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Prerforms checks on an
 * Created by jacob on 9/19/16.
 */
public class AutonomousUploadChecks {

    private Collection<AutonomousValidator> checks = new ArrayList<>();
    private Path baseDir;

    public AutonomousUploadChecks(Path baseDir) {
        this.baseDir = baseDir;
        checks.add(new MissingMappingFileValidator());
    }

    public List<ValidationErrorMessage> getErrors() {
        List<ValidationErrorMessage> errors = new ArrayList<>();

        checks.stream().forEach(
                check -> {
                    ValidationErrorMessage error = check.getCorrespondingError(baseDir);
                    if (error != null) errors.add(error);
                }
        );
        return errors;
    }

    public Collection<AutonomousValidator> getChecks() {
        return checks;
    }

    public void setChecks(Collection<AutonomousValidator> checks) {
        this.checks = checks;
    }
}
