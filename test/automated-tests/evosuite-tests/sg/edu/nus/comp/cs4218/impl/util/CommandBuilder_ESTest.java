/*
 * This file was automatically generated by EvoSuite
 * Fri Mar 17 13:38:31 GMT 2023
 */

package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import static org.evosuite.runtime.EvoAssertions.*;
import org.evosuite.runtime.EvoRunnerJUnit5;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.jupiter.api.extension.RegisterExtension;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true) 
public class CommandBuilder_ESTest extends CommandBuilder_ESTest_scaffolding {
@RegisterExtension
  static EvoRunnerJUnit5 runner = new EvoRunnerJUnit5(CommandBuilder_ESTest.class);

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test0()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      try { 
        CommandBuilder.parseCommand(";;1#zv[6^YS@", applicationRunner0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: Invalid syntax
         //
         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
      }
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test1()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      try { 
        CommandBuilder.parseCommand("| w", applicationRunner0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: Invalid syntax
         //
         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
      }
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test2()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      try { 
        CommandBuilder.parseCommand("U8id<L,/;r#+v\";q9#", applicationRunner0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: Invalid syntax
         //
         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
      }
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test3()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      Command command0 = CommandBuilder.parseCommand("+apGN>}{c", applicationRunner0);
      assertNotNull(command0);
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test4()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      Command command0 = CommandBuilder.parseCommand("Q2_SBR|_A=Y", applicationRunner0);
      assertNotNull(command0);
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test5()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      try { 
        CommandBuilder.parseCommand("B-*DD<kd|", applicationRunner0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: Invalid syntax
         //
         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
      }
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test6()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      Command command0 = CommandBuilder.parseCommand("_r|u-;&9dk}Xt6", applicationRunner0);
      assertNotNull(command0);
  }

  @Test
  @Timeout(value = 4000 , unit = TimeUnit.MILLISECONDS)
  public void test7()  throws Throwable  {
      ApplicationRunner applicationRunner0 = new ApplicationRunner();
      try { 
        CommandBuilder.parseCommand((String) null, applicationRunner0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: Invalid syntax
         //
         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
      }
  }
}
