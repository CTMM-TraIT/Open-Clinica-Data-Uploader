package nl.thehyve.ocdu.autonomous;

import org.junit.*;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 5/30/17.
 */
public class FileCopyServiceTest {

    private static FileCopyService fileCopyService;

    private static final String BASE_DIR = System.getProperty("java.io.tmpdir");

    private static final Path SOURCE_DIR = new File(BASE_DIR + "//source").toPath();

    private static final Path FAILED_DIR = new File(BASE_DIR + "//failed").toPath();

    private static final Path COMPLETED_DIR = new File(BASE_DIR + "//completed").toPath();

    @BeforeClass
    public static void init() throws Exception {
        fileCopyService = new FileCopyService();
        fileCopyService.setSourceDirectory(SOURCE_DIR.toString());
        fileCopyService.setFailedFilesDirectory(FAILED_DIR.toString());
        fileCopyService.setCompletedFilesDirectory(COMPLETED_DIR.toString());

        Path source = createDir(SOURCE_DIR.getFileName().toString());
        fileCopyService.setSourceDirectory(source.toString());

        Path failed = createDir(FAILED_DIR.getFileName().toString());
        fileCopyService.setFailedFilesDirectory(failed.toString());

        Path completed = createDir(COMPLETED_DIR.getFileName().toString());
        fileCopyService.setCompletedFilesDirectory(completed.toString());

        fileCopyService.start();
    }

    private static Path createDir(String dir) throws Exception {

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
        return Files.createTempDirectory(dir, fileAttributes);
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