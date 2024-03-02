package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.impl.app.helper.PasteHelper;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;


@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PasteApplicationBugTest {
    private PasteApplication pasteApplication;
    private ByteArrayInputStream testInputStreamC;
    private List<String> inputArrayFileA;
    private List<String> inputArrayStdinC;

    private String[] testFileNames;

    private String expectedACNoFlag;

    @BeforeEach
    public void init() {
        pasteApplication = new PasteApplication();

        // Constants
        String fileA = "A" + System.lineSeparator() + "B" + System.lineSeparator() + "C" + System.lineSeparator() + "D";
        String fileB = "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator() + "4";
        String stdinC = "E" + System.lineSeparator() + "F" + System.lineSeparator() + "G" + System.lineSeparator() + "H";
        inputArrayFileA = Arrays.asList(fileA.split(System.lineSeparator()));
        inputArrayStdinC = Arrays.asList(stdinC.split(System.lineSeparator()));

        testFileNames = new String[]{"A.txt", "B.txt"};

        testInputStreamC = new ByteArrayInputStream(stdinC.getBytes());
        expectedACNoFlag = "A\tE\nB\tF\nC\tG\nD\tH";
    }

    @Test
    public void mergeFileAndStdin_fileNameSpecifiedWithMultipleStdinNoFlag_returnsFilesAndStdinConcatenated() throws Exception {
        PasteApplication pasteAppSpy = spy(pasteApplication);

        doReturn(String.join(STRING_NEWLINE, inputArrayStdinC))
                .when(pasteAppSpy)
                .mergeStdin(eq(false), eq(testInputStreamC));

        doReturn(String.join(STRING_NEWLINE, inputArrayFileA))
                .when(pasteAppSpy)
                .mergeFile(eq(false), eq(testFileNames[0]));


        List<List<String>> inp = Arrays.asList(inputArrayFileA, inputArrayStdinC);

        try (MockedStatic<PasteHelper> mPasteHelper = mockStatic(PasteHelper.class)) {
            mPasteHelper.when(() -> PasteHelper.concatenateLines(eq(false), eq(inp))).thenReturn(expectedACNoFlag);


            String actual = assertDoesNotThrow(
                    () -> pasteAppSpy.mergeFileAndStdin(false, testInputStreamC, new String[]{"A.txt", "-", "-"})
            );

            verify(pasteAppSpy).mergeStdin(eq(false), eq(testInputStreamC));
            verify(pasteAppSpy).mergeFile(eq(false), eq(testFileNames[0]));

            mPasteHelper.verify(() -> PasteHelper.concatenateLines(eq(false), eq(inp)));

            assertEquals(expectedACNoFlag + STRING_NEWLINE, actual);
        }
    }

}

