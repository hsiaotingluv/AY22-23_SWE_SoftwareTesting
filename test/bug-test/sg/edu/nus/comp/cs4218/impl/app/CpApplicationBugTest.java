package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class CpApplicationBugTest {

    private CpApplication cpApplication;
    private String fileName1;

    @BeforeEach
    void setUp() {
        this.cpApplication = new CpApplication();
        this.fileName1 = "foo.txt";
    }

    @Test
    void cpSrcFileToDestFile_withSameSrcAndDestFile_throwsSameFileException(@TempDir Path tempDir) throws Exception {
        CpApplication cpAppSpy = spy(cpApplication);

        Path filePath1 = tempDir.resolve(fileName1);
        Path src = Files.createFile(filePath1);

        try (MockedStatic<IOUtils> ioUtils = mockStatic(IOUtils.class)) {

            ioUtils.when(() -> IOUtils.resolveFilePath(fileName1))
                    .thenReturn(src);

            Throwable exception = assertThrows(Exception.class, () ->
                    cpAppSpy.cpSrcFileToDestFile(true, fileName1, fileName1));

            ioUtils.verify(() -> IOUtils.resolveFilePath(fileName1), times(2));

            assertEquals(src.getFileName() + " and " + src.getFileName() + " are identical (not copied).",
                    exception.getMessage());
        }
    }

}
