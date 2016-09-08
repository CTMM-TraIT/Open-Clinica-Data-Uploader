package nl.thehyve.ocdu.validators.fileValidators;

import nl.thehyve.ocdu.factories.PatientDataFactory;
import nl.thehyve.ocdu.models.errors.FileFormatError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by piotrzakrzewski on 11/04/16.
 */
public class PatientsFileValidator extends GenericFileValidator{

    private boolean onlyYearOfBirthUsed;

    public PatientsFileValidator(boolean onlyYearOfBirthUsed) {
        super(PatientDataFactory.MANDATORY_HEADERS, new String[]{});
        this.onlyYearOfBirthUsed = onlyYearOfBirthUsed;
    }

    @Override
    public void validateFile(Path file) {
        super.validateFile(file);
        try {
            String header = getHeader(file);
            List<String> allowed = onlyYearOfBirthUsed ?
                    Arrays.asList(PatientDataFactory.ONLY_YEAR_OF_BIRTH_ALL_PERMITTED_COLUMNS) :
                    Arrays.asList(PatientDataFactory.COMPLETE_BIRTH_DATE_ALL_PERMITTED_COLUMNS);
            noOtherColumnsAllowed(header, allowed);
        } catch (IOException e) {
            setValid(false);
            addError(new FileFormatError("Internal Server Error prevented parsing the file. Contact administrator."));
            e.printStackTrace();
        }

    }

    private void noOtherColumnsAllowed(String header, List<String> allowed) {
        List<String> headerSplit = splitLine(header);
        for (String columnName: headerSplit) {
            if (!allowed.contains(columnName)) {
                setValid(false);
                addError(new FileFormatError("Column name not allowed: " + columnName));
            }
        }
    }
}
