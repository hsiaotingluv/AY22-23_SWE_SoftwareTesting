package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExitApplicationTest {

    private ExitApplication exitApplication;

    @BeforeEach
    void setUp() {
        this.exitApplication = new ExitApplication();
    }

    @Test
    void run_withInputsNullStreams_throwsExitException() {
        Throwable exception = assertThrows(ExitException.class, () -> {
            this.exitApplication.run(new String[0], null, null);
        });
        assertEquals(new ExitException("0").getMessage(), exception.getMessage());
    }

    @Test
    void terminateExecution_withNoInputs_throwsExitException() {
        Throwable exception = assertThrows(ExitException.class, () -> {
            this.exitApplication.terminateExecution();
        });
        assertEquals(new ExitException("0").getMessage(), exception.getMessage());
    }
}
