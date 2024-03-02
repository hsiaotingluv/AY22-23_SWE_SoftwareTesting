package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CdApplicationBugTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private CdApplication cdApplication;
    private MvApplication mvApplication;
    private ByteArrayInputStream testInputStream;
    private ByteArrayOutputStream testOutputStream;

    @BeforeEach
    public void init() {
        cdApplication = new CdApplication();
        mvApplication = new MvApplication();
        testInputStream = new ByteArrayInputStream("".getBytes());
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void reset() throws ShellException {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @Test
    void run_cdOutFromDeletedDir_success(@TempDir Path tempDir) throws CdException, ShellException, IOException, MvException {
        String folderName = "exist";
        Path folderPath = tempDir.resolve(folderName);
        Path input = Files.createDirectory(folderPath);
        Path newPath = input.getParent().resolve("deleted");

        Environment.setCurrentDirectory(input.toString());

        // rename directory exist to deleted
        mvApplication.mvSrcFileToDestFile(true, input.toString(), newPath.toString());

        // cd out of a deleted directory
        cdApplication.run(new String[]{".."}, testInputStream, testOutputStream);
        assertEquals(tempDir.toString(), Environment.currentDirectory);
    }

}