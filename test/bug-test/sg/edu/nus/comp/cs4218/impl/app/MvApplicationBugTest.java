package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MvApplicationBugTest {

    private MvApplication mvApplication;

    private String fileName1;
    private String fileName2;
    private List<String> fileText1;
    private List<String> fileText2;

    @BeforeEach
    void init() {
        this.mvApplication = new MvApplication();
        this.fileName1 = "foo.txt";
        this.fileName2 = "foo1.txt";
        this.fileText1 = Arrays.asList("hi");
        this.fileText2 = Arrays.asList("hello");
    }

    @Test
    public void mvSrcFileToDestFile_sameSrcAndDestFile_throwsException(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        Path destFilePath = src.getParent();

        MvException mvException = assertThrows(
                MvException.class,
                () -> mvApplication.mvSrcFileToDestFile(false, src.toString(), destFilePath.toString()));

        String expected = "mv: " + src + " and " + src + " are identical";
        String actual = mvException.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    public void mvFilesToFolder_sameSrcAndDestFile_throwsException(@TempDir Path tempDir) throws Exception {
        String srcFile = fileName1;
        Path srcFilePath = tempDir.resolve(srcFile);
        Path src = Files.createFile(srcFilePath);
        Files.write(src, fileText1);

        String srcFile2 = fileName2;
        Path srcFilePath2 = tempDir.resolve(srcFile2);
        Path src2 = Files.createFile(srcFilePath2);
        Files.write(src2, fileText2);

        assertTrue(Files.exists(src));
        assertTrue(Files.exists(src2));

        Path destFilePath = src.getParent();

        String[] input = new String[] {src.toString(), src2.toString()};

        MvException mvException = assertThrows(
                MvException.class,
                () -> mvApplication.mvFilesToFolder(false, destFilePath.toString(), input));

        String expected = "mv: " + src + " and " + src + " are identical";
        String actual = mvException.getMessage();

        assertEquals(expected, actual);
    }
}
