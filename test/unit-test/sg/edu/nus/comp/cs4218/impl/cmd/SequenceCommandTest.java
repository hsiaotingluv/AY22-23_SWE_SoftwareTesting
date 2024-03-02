package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.INVALID_COMMAND;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.SHELL_EXCEPTION;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.VALID_TEST_STRING;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateCommands;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateInvalidCommands;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateValidAndInvalidCommands;
import static sg.edu.nus.comp.cs4218.CommandTestUtils.generateValidCommandsWithExit;

public class SequenceCommandTest {
    private static final String MOCK_IO_EXCEPTION = "Mock output IO Exception";
    private OutputStream outputStream;
    private InputStream inputStream;

    @BeforeEach
    void setup() {
        inputStream = new ByteArrayInputStream("valid test input stream".getBytes());
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    void evaluate_validCommandStrings_runApplication()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        int numOfCommands = 5;
        List<Command> commands = generateCommands(numOfCommands, inputStream);
        String expectedOutput = Stream.iterate(0, x -> x + 1).limit(numOfCommands)
                .map(x -> String.format(VALID_TEST_STRING, x)).collect(Collectors.joining());

        SequenceCommand testSequence = new SequenceCommand(commands);

        testSequence.evaluate(inputStream, outputStream);

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void evaluate_invalidOutputStream_throwShellException()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        int numOfCommands = 5;
        List<Command> commands = generateCommands(numOfCommands, inputStream);
        String expectedOutput = Stream.iterate(0, x -> x + 1).limit(numOfCommands)
                .map(x -> String.format(VALID_TEST_STRING, x)).collect(Collectors.joining());

        SequenceCommand testSequence = new SequenceCommand(commands);

        Throwable exception = assertThrowsExactly(ShellException.class,
                () -> testSequence.evaluate(inputStream, new OutputStream() {
                    @Override
                    public void write(int unused) throws IOException {
                        throw new IOException(MOCK_IO_EXCEPTION);
                    }
                }));

        assertEquals(String.format(SHELL_EXCEPTION, MOCK_IO_EXCEPTION), exception.getMessage());
    }

    @Test
    void evaluate_invalidCommandStrings_runEvaluate()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<Command> commands = generateInvalidCommands(inputStream);
        String expectedOutput = String.format("%s%s%s", String.format(SHELL_EXCEPTION + System.lineSeparator(), INVALID_COMMAND),
                String.format(SHELL_EXCEPTION + System.lineSeparator(), INVALID_COMMAND),
                String.format(SHELL_EXCEPTION + System.lineSeparator(), INVALID_COMMAND));

        SequenceCommand testSequence = new SequenceCommand(commands);

        testSequence.evaluate(inputStream, outputStream);

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void evaluate_validWithInvalidCommandStrings_runEvaluate()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<Command> commands = generateValidAndInvalidCommands();
        String expectedOutput = String.format("%s%s%s", String.format(VALID_TEST_STRING, 0),
                String.format(SHELL_EXCEPTION + System.lineSeparator(), INVALID_COMMAND),
                String.format(VALID_TEST_STRING, 1));

        SequenceCommand testSequence = new SequenceCommand(commands);

        testSequence.evaluate(inputStream, outputStream);

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void evaluate_validCommandStringsWithExit_throwExitException()
            throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<Command> commands = generateValidCommandsWithExit(inputStream);
        String expectedOutput = String.format("%s%s", String.format(VALID_TEST_STRING, 0),
                String.format(VALID_TEST_STRING, 1));

        SequenceCommand testSequence = new SequenceCommand(commands);

        assertThrowsExactly(ExitException.class, () -> testSequence.evaluate(inputStream, outputStream));

        assertEquals(expectedOutput, outputStream.toString());
    }

}
