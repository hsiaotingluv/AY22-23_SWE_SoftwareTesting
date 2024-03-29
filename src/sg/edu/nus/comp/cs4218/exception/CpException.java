package sg.edu.nus.comp.cs4218.exception;

public class CpException extends AbstractApplicationException {
    public CpException(String message) {
        super("cp: " + message);
    }

    public CpException(String message, Throwable cause) {
        super("cp: " + message, cause);
    }
}
