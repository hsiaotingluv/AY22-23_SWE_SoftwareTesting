package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;
import sg.edu.nus.comp.cs4218.impl.app.MvApplication;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;
import sg.edu.nus.comp.cs4218.impl.app.TeeApplication;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.app.WcApplication;

import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.E_INVALID_APP;


public class ApplicationRunner {
    public final static String APP_LS = "ls";
    public final static String APP_WC = "wc";
    public final static String APP_ECHO = "echo";
    public final static String APP_EXIT = "exit";
    public final static String APP_GREP = "grep";
    public final static String APP_PASTE = "paste";
    public final static String APP_MV = "mv";
    public final static String APP_UNIQ = "uniq";
    public final static String APP_CD = "cd";
    public final static String APP_CAT = "cat";
    public final static String APP_CP = "cp";
    public final static String APP_CUT = "cut";
    public final static String APP_RM = "rm";
    public final static String APP_SORT = "sort";
    public final static String APP_TEE = "tee";

    /**
     * Run the application as specified by the application command keyword and arguments.
     *
     * @param app          String containing the keyword that specifies what application to run.
     * @param argsArray    String array containing the arguments to pass to the applications for
     *                     running.
     * @param inputStream  InputStream for the application to get input from, if needed.
     * @param outputStream OutputStream for the application to write its output to.
     * @throws AbstractApplicationException If an exception happens while running an application.
     * @throws ShellException               If an unsupported or invalid application command is
     *                                      detected.
     */
    public void runApp(String app, String[] argsArray, InputStream inputStream,
                       OutputStream outputStream)
            throws AbstractApplicationException, ShellException {
        Application application = getApplication(app);
        application.run(argsArray, inputStream, outputStream);
    }

    public Application getApplication(String app) throws ShellException {
        switch (app) {
            case APP_WC:
                return new WcApplication();
            case APP_ECHO:
                return new EchoApplication();
            case APP_EXIT:
                return new ExitApplication();
            case APP_GREP:
                return new GrepApplication();
            case APP_CD:
                return new CdApplication();
            case APP_CAT:
                return new CatApplication();
            case APP_CP:
                return new CpApplication();
            case APP_CUT:
                return new CutApplication();
            case APP_RM:
                return new RmApplication();
            case APP_SORT:
                return new SortApplication();
            case APP_TEE:
                return new TeeApplication();
            case APP_LS:
                return new LsApplication();
            case APP_MV:
                return new MvApplication();
            case APP_PASTE:
                return new PasteApplication();
            case APP_UNIQ:
                return new UniqApplication();
            default:
                throw new ShellException(app + ": " + E_INVALID_APP);
        }
    }
}
