package nl.thehyve.ocdu.autonomous;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by jacob on 5/30/17.
 */
public class FileCopyServiceTest {

    private static FileCopyService fileCopyService;

    private static final String BASE_DIR = "/home/foo/bar";

    @Before
    public void init() throws Exception {
        fileCopyService = new FileCopyService();
        fileCopyService.setSourceDirectory(BASE_DIR + "/source");
        fileCopyService.setFailedFilesDirectory(BASE_DIR + "/failed");
        fileCopyService.setCompletedFilesDirectory(BASE_DIR + "/completed");
    }

    @Test
    public void testAddTimeStampToDestinationPath_NoSubDirectories() {
        Path pathToCopy = new File(fileCopyService.getSourceDirectory() + "/testfile.txt").toPath();
        Path result = fileCopyService.addTimeStampToDestinationPath(pathToCopy, fileCopyService.getCompletedFilesDirectory());
        String resultPath = result.toAbsolutePath().toString();
        Assert.assertTrue(resultPath.endsWith("testfile.txt"));
    }

    @Test
    public void testAddTimeStampToDestinationPath_WithSubDirectories() {
        Path pathToCopy = new File(fileCopyService.getSourceDirectory() + "/StudyA/testfile.txt").toPath();
        Path result = fileCopyService.addTimeStampToDestinationPath(pathToCopy, fileCopyService.getCompletedFilesDirectory());
        String resultPath = result.toAbsolutePath().toString();
        Assert.assertTrue(resultPath.endsWith("testfile.txt"));
        Assert.assertTrue(resultPath.contains("StudyA"));
        Assert.assertTrue(resultPath.endsWith("testfile.txt"));
    }
}