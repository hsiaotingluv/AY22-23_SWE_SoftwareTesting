/**
 * Scaffolding file used to store all the setups needed to run 
 * tests automatically generated by EvoSuite
 * Sat Mar 18 17:28:27 GMT 2023
 */

package sg.edu.nus.comp.cs4218.impl.cmd;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;

@EvoSuiteClassExclude
public class CallCommand_ESTest_scaffolding {

  @org.junit.jupiter.api.extension.RegisterExtension
  public org.evosuite.runtime.vnet.NonFunctionalRequirementExtension nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementExtension();

  private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone(); 

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);


  @BeforeAll
  public static void initEvoSuiteFramework() { 
    org.evosuite.runtime.RuntimeSettings.className = "sg.edu.nus.comp.cs4218.impl.cmd.CallCommand"; 
    org.evosuite.runtime.GuiSupport.initialize(); 
    org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100; 
    org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000; 
    org.evosuite.runtime.RuntimeSettings.mockSystemIn = true; 
    org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED; 
    org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT(); 
    org.evosuite.runtime.classhandling.JDKClassResetter.init();
    setSystemProperties();
    initializeClasses();
    org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
  } 

  @AfterAll
  public static void clearEvoSuiteFramework(){ 
    Sandbox.resetDefaultSecurityManager(); 
    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone()); 
  } 

  @BeforeEach
  public void initTestCase(){ 
    threadStopper.storeCurrentThreads();
    threadStopper.startRecordingTime();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().initHandler(); 
    org.evosuite.runtime.sandbox.Sandbox.goingToExecuteSUTCode(); 
    setSystemProperties(); 
    org.evosuite.runtime.GuiSupport.setHeadless(); 
    org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
    org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
  } 

  @AfterEach
  public void doneWithTestCase(){ 
    threadStopper.killAndJoinClientThreads();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().safeExecuteAddedHooks(); 
    org.evosuite.runtime.classhandling.JDKClassResetter.reset(); 
    resetClasses(); 
    org.evosuite.runtime.sandbox.Sandbox.doneWithExecutingSUTCode(); 
    org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 
    org.evosuite.runtime.GuiSupport.restoreHeadlessMode(); 
  } 

  public static void setSystemProperties() {
 
    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone()); 
    java.lang.System.setProperty("user.dir", "C:\\Users\\lkl74\\Downloads\\nus\\CS4218\\cs4218-project-2023-team27"); 
    java.lang.System.setProperty("java.io.tmpdir", "C:\\Users\\lkl74\\AppData\\Local\\Temp\\"); 
  }

  private static void initializeClasses() {
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(CallCommand_ESTest_scaffolding.class.getClassLoader() ,
      "sg.edu.nus.comp.cs4218.impl.app.LsApplication$InvalidDirectoryException",
      "sg.edu.nus.comp.cs4218.exception.AbstractApplicationException",
      "sg.edu.nus.comp.cs4218.exception.ShellException",
      "sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser",
      "sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser",
      "sg.edu.nus.comp.cs4218.impl.app.WcApplication",
      "sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler",
      "sg.edu.nus.comp.cs4218.impl.app.LsApplication",
      "sg.edu.nus.comp.cs4218.app.WcInterface",
      "sg.edu.nus.comp.cs4218.impl.util.RegexArgument",
      "sg.edu.nus.comp.cs4218.Command",
      "sg.edu.nus.comp.cs4218.app.LsInterface",
      "sg.edu.nus.comp.cs4218.exception.LsException",
      "sg.edu.nus.comp.cs4218.impl.cmd.CallCommand",
      "sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner",
      "sg.edu.nus.comp.cs4218.impl.util.IOUtils",
      "sg.edu.nus.comp.cs4218.impl.util.StringUtils",
      "sg.edu.nus.comp.cs4218.exception.WcException",
      "sg.edu.nus.comp.cs4218.impl.parser.ArgsParser",
      "sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver",
      "sg.edu.nus.comp.cs4218.exception.InvalidArgsException",
      "sg.edu.nus.comp.cs4218.Environment",
      "sg.edu.nus.comp.cs4218.Application"
    );
  } 

  private static void resetClasses() {
    org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(CallCommand_ESTest_scaffolding.class.getClassLoader()); 

    org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
      "sg.edu.nus.comp.cs4218.impl.cmd.CallCommand",
      "sg.edu.nus.comp.cs4218.impl.util.StringUtils",
      "sg.edu.nus.comp.cs4218.impl.util.CommandBuilder",
      "sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner",
      "sg.edu.nus.comp.cs4218.exception.ShellException",
      "sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler",
      "sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver",
      "sg.edu.nus.comp.cs4218.impl.util.RegexArgument",
      "sg.edu.nus.comp.cs4218.impl.app.MvApplication",
      "sg.edu.nus.comp.cs4218.exception.AbstractApplicationException",
      "sg.edu.nus.comp.cs4218.exception.MvException",
      "sg.edu.nus.comp.cs4218.impl.app.LsApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.ArgsParser",
      "sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser",
      "sg.edu.nus.comp.cs4218.Environment",
      "sg.edu.nus.comp.cs4218.impl.util.IOUtils",
      "sg.edu.nus.comp.cs4218.impl.app.CatApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser",
      "sg.edu.nus.comp.cs4218.exception.CatException",
      "sg.edu.nus.comp.cs4218.impl.app.EchoApplication",
      "sg.edu.nus.comp.cs4218.exception.EchoException",
      "sg.edu.nus.comp.cs4218.impl.app.GrepApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser",
      "sg.edu.nus.comp.cs4218.exception.InvalidArgsException",
      "sg.edu.nus.comp.cs4218.exception.GrepException",
      "sg.edu.nus.comp.cs4218.impl.app.SortApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser",
      "sg.edu.nus.comp.cs4218.impl.app.SortApplication$1",
      "sg.edu.nus.comp.cs4218.exception.SortException",
      "sg.edu.nus.comp.cs4218.impl.app.CdApplication",
      "sg.edu.nus.comp.cs4218.impl.app.CpApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser",
      "sg.edu.nus.comp.cs4218.exception.CpException",
      "sg.edu.nus.comp.cs4218.impl.app.RmApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser",
      "sg.edu.nus.comp.cs4218.exception.RmException",
      "sg.edu.nus.comp.cs4218.exception.CdException",
      "sg.edu.nus.comp.cs4218.impl.app.CutApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser",
      "sg.edu.nus.comp.cs4218.exception.CutException",
      "sg.edu.nus.comp.cs4218.impl.app.UniqApplication",
      "sg.edu.nus.comp.cs4218.exception.UniqException",
      "sg.edu.nus.comp.cs4218.impl.app.LsApplication$InvalidDirectoryException",
      "sg.edu.nus.comp.cs4218.impl.app.WcApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser",
      "sg.edu.nus.comp.cs4218.exception.LsException",
      "sg.edu.nus.comp.cs4218.impl.app.PasteApplication",
      "sg.edu.nus.comp.cs4218.exception.PasteException",
      "sg.edu.nus.comp.cs4218.impl.app.TeeApplication",
      "sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser",
      "sg.edu.nus.comp.cs4218.exception.TeeException",
      "sg.edu.nus.comp.cs4218.exception.WcException"
    );
  }
}