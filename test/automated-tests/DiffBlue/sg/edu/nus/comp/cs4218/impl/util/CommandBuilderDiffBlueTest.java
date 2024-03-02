package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;

class CommandBuilderDiffBlueTest {
    /**
     * Method under test: {@link CommandBuilder#parseCommand(String, ApplicationRunner)}
     */
    @Test
    void testParseCommand() throws ShellException {
        List<String> argsList = ((CallCommand) CommandBuilder.parseCommand("Command String", new ApplicationRunner()))
                .getArgsList();
        assertEquals(2, argsList.size());
        assertEquals("Command", argsList.get(0));
        assertEquals("String", argsList.get(1));
    }

    /**
     * Method under test: {@link CommandBuilder#parseCommand(String, ApplicationRunner)}
     */
    @Test
    void testParseCommand2() throws ShellException {
        assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand("([^'\"`|<>;\\s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+",
                        new ApplicationRunner()));
    }

    /**
     * Method under test: {@link CommandBuilder#parseCommand(String, ApplicationRunner)}
     */
    @Test
    void testParseCommand3() throws ShellException {
        assertThrows(ShellException.class, () -> CommandBuilder.parseCommand("", new ApplicationRunner()));
    }
}

