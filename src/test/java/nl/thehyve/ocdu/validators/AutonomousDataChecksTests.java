package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the autonomous validation
 * Created by jacob on 9/21/16.
 */
public class AutonomousDataChecksTests {

    @Test
    public void testMissingMappingFile() throws Exception {
        Path baseDirectory = Files.createTempDirectory("TEMP");

        Path directoryMappingFileAbsent = Files.createTempDirectory(baseDirectory, "mapping-absent");
        Path directoryMappingPresent = Files.createTempDirectory(baseDirectory, "mapping-present");
        Files.createFile(directoryMappingPresent.resolve("mapping.yml"));

        AutonomousUploadChecks autonomousUploadChecks = new AutonomousUploadChecks(baseDirectory);

        List<ValidationErrorMessage> validationErrorMessageList =
                autonomousUploadChecks.getErrors();

        assertEquals(1,validationErrorMessageList.size());
        ArrayList<String> message = (ArrayList<String>) validationErrorMessageList.get(0).getOffendingValues();
        assertEquals(true, StringUtils.contains(message.get(0), directoryMappingFileAbsent.toString()));
    }
}
