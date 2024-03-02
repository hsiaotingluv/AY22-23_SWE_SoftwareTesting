package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = -742723164724927309L;

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(String message, Throwable cause) {
        super("mv: " + message, cause);
    }
}
