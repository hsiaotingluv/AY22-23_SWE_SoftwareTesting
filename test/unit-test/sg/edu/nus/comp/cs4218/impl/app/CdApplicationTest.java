package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_NULL_ARGS;

/**
 * Provides unit tests of CdApplication
 * <p>
 * Positive test cases:
 * - cd with a single argument of absolute path
 * - cd with a single argument of relative path
 * - cd with multiple arguments of relative path (e.g. folder1/folder2)
 * - cd without argument (should return to home directory)
 * - cd with empty string argument (should return to home directory)
 * <p>
 * Negative test cases:
 * - cd with null argument (throws null argument exception)
 * - cd with null input stream (throws null input stream exception)
 * - cd with null output stream (throws null output stream exception)
 * - cd with multiple arguments (throws file not found exception)
 * - cd with non-existing directory (throws file not found exception)
 * - cd with no read permission directory (throws no permission exception)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CdApplicationTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private CdApplication cdApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;

    @BeforeEach
    public void init() {
        cdApplication = new CdApplication();
        testInputStream = new ByteArrayInputStream("".getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void reset() throws ShellException {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    /**
     * cd with a single argument of absolute path
     */
    @Test
    void cd_singleAbsolutePath_cdToCorrectDirectory(@TempDir Path tempDir) throws CdException, IOException {
        String folderName = "validFolder_returnCorrectDirectory";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        CdApplication cdAppSpy = spy(cdApplication);

        // run cd app with absolute path to cd into test folder
        cdAppSpy.run(new String[]{input.toString()}, testInputStream, testOutputStream);

        // verify current directory has successfully cd into test folder
        assertEquals(input.toString(), Environment.currentDirectory);
    }

    /**
     * cd with a single argument of relative path
     */
    @Test
    void cd_singleRelativePath_cdToCorrectDirectory(@TempDir Path tempDir) throws CdException, IOException, ShellException {
        String folderName = "validFolder_returnCorrectDirectory";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        CdApplication cdAppSpy = spy(cdApplication);
        Environment.setCurrentDirectory(tempDir.toString());

        // run cd app with relative path to cd into test folder
        cdAppSpy.run(new String[]{folderName}, testInputStream, testOutputStream);

        // verify current directory has successfully cd into test folder
        assertEquals(input.toString(), Environment.currentDirectory);
    }

    /**
     * cd with multiple arguments of relative path (e.g. folder1/folder2)
     */
    @Test
    void cd_multipleRelativePath_cdToCorrectDirectory(@TempDir Path tempDir) throws CdException, IOException, ShellException {
        String folderName = "validMultipleFolder_returnCorrectDirectory";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        String folderName2 = "validMultipleFolder2_returnCorrectDirectory";
        Path folderPath2 = input.resolve(folderName2);
        Path input2 = Files.createDirectory(folderPath2);

        CdApplication cdAppSpy = spy(cdApplication);
        Environment.setCurrentDirectory(tempDir.toString());


        // run cd app with relative path to cd into valid folder 1 and then into valid folder 2
        cdAppSpy.run(new String[]{folderName + StringUtils.fileSeparator() + folderName2},
                testInputStream, testOutputStream);

        // verify current directory has successfully cd into cdTestFolder2
        assertEquals(input2.toString(), Environment.currentDirectory);
    }

    /**
     * cd without argument (should return to home directory)
     */
    @Test
    void cd_noArgument_cdToUserDirectory() throws CdException {
        CdApplication cdAppSpy = spy(cdApplication);

        // run cd app with relative path to cd into testFolder
        cdAppSpy.run(new String[]{}, testInputStream, testOutputStream);

        // verify current directory has successfully cd into testFolder
        assertEquals(System.getProperty("user.dir").trim(), Environment.currentDirectory);
    }

    /**
     * cd with empty string argument (should return to home directory)
     */
    @Test
    void cd_emptyStringArgument_cdToHomeDirectory() throws CdException {
        // run cd app with relative path to cd into testFolder
        cdApplication.run(new String[]{""}, testInputStream, testOutputStream);

        // verify current directory has successfully cd into testFolder
        assertEquals(System.getProperty("user.dir").trim(), Environment.currentDirectory);
    }

    /**
     * cd with null argument (throws null argument exception)
     */
    @Test
    void cd_nullArgument_throwsNullArgumentException() {
        CdApplication cdAppSpy = spy(cdApplication);

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(null, testInputStream, testOutputStream));
        assertEquals("cd: " + E_NULL_ARGS, cdException.getMessage());
    }

    /**
     * cd with null input stream (throws no input stream exception)
     */
    @Test
    void cd_nullInputStream_throwsNoInputStreamException(@TempDir Path tempDir) throws IOException {
        String folderName = "validFolderWithNullInputStream_throwsException";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        CdApplication cdAppSpy = spy(cdApplication);

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(new String[]{input.toString()}, null, testOutputStream));
        assertEquals("cd: " + E_NO_ISTREAM, cdException.getMessage());
    }

    /**
     * cd with null output stream (throws no output stream exception)
     */
    @Test
    void cd_nullOutputStream_throwsNoOutputStreamException(@TempDir Path tempDir) throws IOException {
        String folderName = "validFolderWithNullOutputStream_throwsException";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        CdApplication cdAppSpy = spy(cdApplication);

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(new String[]{input.toString()}, testInputStream, null));
        assertEquals("cd: " + E_NO_OSTREAM, cdException.getMessage());
    }

    /**
     * cd with multiple arguments (throws file not found exception)
     */
    @Test
    void cd_multipleArguments_throwsFileNotFoundException(@TempDir Path tempDir) throws IOException, ShellException {
        String folderName = "invalidMultipleFolder1_throwsException";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);

        String folderName2 = "invalidMultipleFolder2_throwsException";
        Path folderPath2 = tempDir.resolve(folderName2);
        Path input2 = Files.createDirectory(folderPath2);

        CdApplication cdAppSpy = spy(cdApplication);
        Environment.setCurrentDirectory(tempDir.toString());

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(new String[]{folderName + " " + folderName2}, testInputStream, testOutputStream));
        assertEquals("cd: " + E_FILE_NOT_FOUND, cdException.getMessage());
    }

    /**
     * cd with non-existing directory (throws file not found exception)
     */
    @Test
    void cd_nonExistingDirectory_throwsFileNotFoundException() {
        CdApplication cdAppSpy = spy(cdApplication);

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(new String[]{"nonExistingDir"}, testInputStream, testOutputStream));
        assertEquals("cd: " + E_FILE_NOT_FOUND, cdException.getMessage());
    }

    /**
     * cd with no read permission directory (throws no permission exception)
     */
    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void cd_noReadPermissionDirectory_throwsNoPermissionException(@TempDir Path tempDir) throws IOException {
        String folderName = "noReadPermissionFolder_throwsException";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);
        input.toFile().setExecutable(false);

        CdApplication cdAppSpy = spy(cdApplication);

        // verify exception thrown
        CdException cdException = assertThrows(
                CdException.class,
                () -> cdAppSpy.run(new String[]{input.toString()}, testInputStream, testOutputStream));
        assertEquals("cd: " + E_NO_PERM, cdException.getMessage());
    }

}