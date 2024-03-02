package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

public class IORedirectionHandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {"<", ">"})
    void isRedirOperator_validOperators_returnTrue(String input)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = IORedirectionHandler.class
                .getDeclaredMethod("isRedirOperator", String.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(IORedirectionHandler.class, input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<<", ">>", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "[", "]", "{", "}", "\\",
            "|", "'", "\"", ",", ".", "/", "?", "`", "~", ";", ":", "\n", "\r", "\t", "a", "1", "\f", "\0", " "})
    void isRedirOperator_invalidOperators_returnFalse(String input)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = IORedirectionHandler.class
                .getDeclaredMethod("isRedirOperator", String.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(IORedirectionHandler.class, input));
    }


    @Nested
    class ExtractRedirOptions {
        private InputStream origInputStream;
        private OutputStream origOutputStream;
        private ArgumentResolver argsResolver;

        @BeforeEach
        void setUp() {
            origInputStream = new ByteArrayInputStream("Original Stream".getBytes());
            origOutputStream = new ByteArrayOutputStream();
            argsResolver = mock(ArgumentResolver.class);
        }

        @Test
        void extractRedirOptions_validArguments_noExceptions()
                throws ShellException, IOException, AbstractApplicationException {
            try (MockedStatic<IOUtils> unused = mockStatic(IOUtils.class)) {
                try (MockedStatic<ArgumentResolver> unused2 = mockStatic(ArgumentResolver.class)) {
                    String testFilename = "someInputFile";
                    byte[] inBytes = "Valid Test InputStream".getBytes();
                    byte[] outBytes = "Valid Test OutputStream".getBytes();
                    ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
                    when(IOUtils.openInputStream(any())).thenReturn(new ByteArrayInputStream(inBytes));
                    when(IOUtils.openOutputStream(any())).thenReturn(testOutputStream);
                    List<String> args = List.of("someArgs", "moreArgs");
                    List<String> redirects = List.of("<", testFilename, ">", "someOutputFile");
                    List<String> argsList = new ArrayList<>();
                    argsList.addAll(args);
                    argsList.addAll(redirects);

                    when(ArgumentResolver.resolveOneArgument(any())).thenReturn(List.of(testFilename));
                    IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                            origOutputStream);

                    assertDoesNotThrow(ioRedirect::extractRedirOptions);
                    assertIterableEquals(ioRedirect.getNoRedirArgsList(), args);
                    assertArrayEquals(ioRedirect.getInputStream().readAllBytes(), inBytes);
                    ioRedirect.getOutputStream().write(outBytes);
                    assertArrayEquals(testOutputStream.toByteArray(), outBytes);
                }
            }
        }

        @Test
        void extractRedirOptions_invalidFileArgument_throwShellException()
                throws ShellException, FileNotFoundException, AbstractApplicationException {
            try (MockedStatic<IOUtils> unused = mockStatic(IOUtils.class)) {
                try (MockedStatic<ArgumentResolver> unused2 = mockStatic(ArgumentResolver.class)) {
                    when(IOUtils.openInputStream(any())).thenReturn(new ByteArrayInputStream("Valid InputStream".getBytes()));
                    String invalidFileName = "someOtherFile SomeMoreFiles";
                    List<String> argsList = List.of("someArg", "moreArg", "<", invalidFileName);
                    when(ArgumentResolver.resolveOneArgument(any())).thenReturn(Arrays.asList(invalidFileName.split(" ")));
                    IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                            origOutputStream);

                    assertThrowsExactly(ShellException.class, ioRedirect::extractRedirOptions, E_SYNTAX);
                }
            }
        }

        @Test
        void extractRedirOptions_nullArgsList_throwShellException() {
            List<String> argsList = null;
            IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                    origOutputStream);

            assertThrowsExactly(ShellException.class, ioRedirect::extractRedirOptions, E_SYNTAX);
        }

        @Test
        void extractRedirOptions_emptyArgsList_throwShellException() {
            List<String> argsList = new ArrayList<>();
            IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                    origOutputStream);

            assertThrowsExactly(ShellException.class, ioRedirect::extractRedirOptions, E_SYNTAX);
        }

        @ParameterizedTest
        @ValueSource(strings = {"<", ">"})
        void extractRedirOptions_invalidFilename_throwShellException(String input) {
            List<String> argsList = List.of("someArgs", "moreArgs", "<", input, "someOtherFile");
            IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                    origOutputStream);

            assertThrowsExactly(ShellException.class, ioRedirect::extractRedirOptions, E_INVALID_FILE);
        }

        @Test
        void extractRedirOptions_chainIoRedirection_noExceptions()
                throws ShellException, IOException, AbstractApplicationException {
            try (MockedStatic<IOUtils> unused = mockStatic(IOUtils.class)) {
                try (MockedStatic<ArgumentResolver> unused2 = mockStatic(ArgumentResolver.class)) {
                    byte[] inBytes = "Valid Test InputStream".getBytes();
                    byte[] inBytes2 = "Valid Test InputStream 2".getBytes();
                    when(IOUtils.openInputStream(any()))
                            .thenReturn(new ByteArrayInputStream(inBytes))
                            .thenReturn(new ByteArrayInputStream(inBytes2));

                    List<String> argsList = List.of("someArgs", "moreArgs", "<", "someOtherFile");
                    when(ArgumentResolver.resolveOneArgument(any())).thenReturn(List.of("someOtherFile"));
                    IORedirectionHandler ioRedirect = new IORedirectionHandler(argsList, origInputStream,
                            origOutputStream);
                    assertDoesNotThrow(ioRedirect::extractRedirOptions);

                    IORedirectionHandler newIoRedirect = new IORedirectionHandler(argsList, ioRedirect.getInputStream(),
                            ioRedirect.getOutputStream());

                    assertDoesNotThrow(newIoRedirect::extractRedirOptions);
                    assertArrayEquals(newIoRedirect.getInputStream().readAllBytes(), inBytes2);
                }
            }
        }
    }
}
