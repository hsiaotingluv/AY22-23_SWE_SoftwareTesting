package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExitApplicationPublicIntegrationTest {

    public static final String EXIT_MSG = "exit: 0";

    @Test
    void run_noArgument_exitsSuccessfully() {
        ExitException thrown = assertThrows(ExitException.class, () -> new ExitApplication().run(null, null, null));
        assertEquals(thrown.getMessage(), EXIT_MSG);
    }

    @Test
    void run_withArgument_exitsSuccessfully() {
        String[] args = {"something"};
        ExitException thrown = assertThrows(ExitException.class, () -> new ExitApplication().run(args, null, null));
        assertEquals(thrown.getMessage(), EXIT_MSG);
    }
}
