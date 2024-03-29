package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_SYNTAX;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;

    public CallCommand(List<String> argsList, ApplicationRunner appRunner) {
        this.argsList = argsList;
        this.appRunner = appRunner;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(E_SYNTAX);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = getIORedirection(stdin, stdout);
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream();
        OutputStream outputStream = redirHandler.getOutputStream();

        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = ArgumentResolver.parseArguments(noRedirArgsList);
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }

        IOUtils.closeInputStream(inputStream);
        IOUtils.closeOutputStream(outputStream);

    }

    protected IORedirectionHandler getIORedirection(InputStream stdin, OutputStream stdout) throws FileNotFoundException, AbstractApplicationException, ShellException {
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout);
        try {
            redirHandler.extractRedirOptions();
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            IOUtils.closeInputStream(redirHandler.getInputStream());
            IOUtils.closeOutputStream(redirHandler.getOutputStream());
            throw e;
        }
        return redirHandler;
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }
}
