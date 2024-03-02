package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.INVALID_COMMAND;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.SHELL_EXCEPTION;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateCallCommands;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateValidAndInvalidCallCommands;

public class PipeCommandTest {

    private static final String TEST_INPUT_STRING = "valid test input stream";
    private OutputStream outputStream;
    private InputStream inputStream;

    @BeforeEach
    void setup() {
        inputStream = new ByteArrayInputStream(TEST_INPUT_STRING.getBytes());
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    void evaluate_validCommandStrings_runApplication()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        int numOfCommands = 5;
        List<CallCommand> commands = generateCallCommands(numOfCommands);

        PipeCommand testSequence = new PipeCommand(commands);

        testSequence.evaluate(inputStream, outputStream);

        assertEquals(TEST_INPUT_STRING, outputStream.toString());
    }

    @Test
    void evaluate_invalidCommandString_throwShellException()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<CallCommand> commands = generateValidAndInvalidCallCommands(new ShellException(INVALID_COMMAND));

        PipeCommand testSequence = new PipeCommand(commands);

        Throwable exception = assertThrowsExactly(ShellException.class, () -> testSequence.evaluate(inputStream, outputStream));

        verify(commands.get(2), never()).evaluate(any(), any());

        assertEquals(String.format(SHELL_EXCEPTION, INVALID_COMMAND), exception.getMessage());
    }

    @Test
    void evaluate_invalidCommandString_throwAbstractApplicationException()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<CallCommand> commands = generateValidAndInvalidCallCommands(new AbstractApplicationException(INVALID_COMMAND) {
        });

        PipeCommand testSequence = new PipeCommand(commands);

        Throwable exception = assertThrows(AbstractApplicationException.class, () -> testSequence.evaluate(inputStream, outputStream));

        verify(commands.get(2), never()).evaluate(any(), any());

        assertEquals(String.format(INVALID_COMMAND), exception.getMessage());
    }
}
