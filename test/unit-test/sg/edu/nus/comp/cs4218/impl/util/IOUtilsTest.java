package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;


public class IOUtilsTest {

    @Test
    void openInputStream_validFilename_returnCorrectInputStream(@TempDir Path tempDir) throws IOException {
        String filename = "openInputStream_validFilename_returnCorrectInputStream.txt";
        Path fullPathname = tempDir.resolve(filename);
        Path input = Files.createFile(fullPathname);
        assertNotNull(input);
        assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(fullPathname.toString());
            inputStream.close();
        });
    }

    @Test
    void openInputStream_invalidFilename_throwShellException(@TempDir Path tempDir) {
        String filename = "openInputStream_invalidFilename_throwShellException.txt";
        Path fullPathname = tempDir.resolve(filename);
        assertThrowsExactly(ShellException.class, () -> IOUtils.openInputStream(fullPathname.toString()));
    }

    @Test
    void openOutputStream_validFilename_returnCorrectOutputStream(@TempDir Path tempDir) {
        String filename = "openOutputStream_validFilename_returnCorrectOutputStream.txt";
        Path fullPathname = tempDir.resolve(filename);
        assertDoesNotThrow(() -> {
            OutputStream outputStream = IOUtils.openOutputStream(fullPathname.toString());
            outputStream.close();
        });
    }

    @Test
    void openOutputStream_invalidDirectory_throwShellException(@TempDir Path tempDir) {
        String filename = "test" + File.separator + "openOutputStream_invalidDirectory_throwShellException.txt";
        Path fullPathname = tempDir.resolve(filename);
        assertThrowsExactly(ShellException.class, () -> IOUtils.openOutputStream(fullPathname.toString()));
    }

    @Test
    void closeInputStream_validInputStream_closeStreamSuccessfully() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = new ByteArrayInputStream("Valid InputStream".getBytes());
            IOUtils.closeInputStream(inputStream);
        });
    }

    @Test
    void closeInputStream_invalidInputStream_throwShellException() {
        assertThrowsExactly(ShellException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() {
                    return 0;
                }

                @Override
                public void close() throws IOException {
                    throw new IOException();
                }
            };
            IOUtils.closeInputStream(inputStream);
        });
    }

    @Test
    void closeOutputStream_validOutputStream_closeStreamSuccessfully(@TempDir Path tempDir) {
        String filename = "closeOutputStream_validOutputStream_closeStreamSuccessfully.txt";
        Path fullPathname = tempDir.resolve(filename);
        assertDoesNotThrow(() -> {
            OutputStream outputStream = new FileOutputStream(fullPathname.toString());
            IOUtils.closeOutputStream(outputStream);
        });
    }

    @Test
    void closeOutputStream_invalidInputStream_throwShellException() {
        assertThrowsExactly(ShellException.class, () -> {
            OutputStream outputStream = new OutputStream() {
                @Override
                public void write(int bytes) throws IOException {
                    throw new IOException();
                }

                @Override
                public void close() throws IOException {
                    throw new IOException();
                }
            };
            IOUtils.closeOutputStream(outputStream);
        });
    }

    @Test
    void resolveFilePath_validFilename_returnCorrectPath() {
        String filename = "test" + File.separator + "resolveFilePath_validFilename_returnCorrectPath.txt";
        assertDoesNotThrow(() -> IOUtils.resolveFilePath(filename));
    }

    @Test
    void resolveFilePath_invalidFilename_throwInvalidPathException() {
        String filename = "test" + File.separator + "\0.txt";
        assertThrowsExactly(ShellException.class, () -> IOUtils.resolveFilePath(filename));
    }

    @Test
    void getLinesFromInputStream_validInputStream_returnCorrectOutput() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = new ByteArrayInputStream("Valid InputStream\nCorrect Output\n".getBytes());
            assertIterableEquals(IOUtils.getLinesFromInputStream(inputStream),
                    List.of("Valid InputStream", "Correct Output"));
            inputStream.close();
        });
    }

    @Test
    void getLinesFromInputStream_invalidInputStream_throwShellException() {
        assertThrowsExactly(ShellException.class, () -> {
            InputStream inputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            };
            IOUtils.getLinesFromInputStream(inputStream);
        });
    }
}
