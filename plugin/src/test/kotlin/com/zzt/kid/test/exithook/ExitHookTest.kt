package com.zzt.kid.test.exithook

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.test.captureOutput
import com.zzt.kid.test.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExitHookTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test ExitHook called after original Unit method`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.ExitHook

        fun main() {
          val logger = Logger()
          logger.log("test_message")
        }

        object LoggerHook {
          @ExitHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun onExitLog(logger: Logger, msg: String) {
            println("EXIT_HOOK_CALLED:${'$'}msg")
          }
        }

        class Logger {
          fun log(msg: String) { println("ORIGINAL_CALLED:${'$'}msg") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("ORIGINAL_CALLED:test_message" in output, "Original should execute first. Output: $output")
    assertTrue("EXIT_HOOK_CALLED:test_message" in output, "Exit hook should be called after. Output: $output")
    val originalIndex = output.indexOf("ORIGINAL_CALLED")
    val exitIndex = output.indexOf("EXIT_HOOK_CALLED")
    assertTrue(originalIndex < exitIndex, "Original should run before exit hook. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test ExitHook called after original method with multiple statements`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.ExitHook

        fun main() {
          val service = Service()
          service.process("step1", "step2")
        }

        object ServiceHook {
          @ExitHook(
            className = "Service",
            methodName = "process",
            paramsTypes = "(kotlin.Stringkotlin.String)"
          )
          fun onExitProcess(service: Service, a: String, b: String) {
            println("EXIT:${'$'}a:${'$'}b")
          }
        }

        class Service {
          fun process(a: String, b: String) {
            println("STEP1:${'$'}a")
            println("STEP2:${'$'}b")
          }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("STEP1:step1" in output, "First statement should execute. Output: $output")
    assertTrue("STEP2:step2" in output, "Second statement should execute. Output: $output")
    assertTrue("EXIT:step1:step2" in output, "Exit hook should be called with params. Output: $output")
    val step2Index = output.indexOf("STEP2")
    val exitIndex = output.indexOf("EXIT")
    assertTrue(step2Index < exitIndex, "All body statements run before exit hook. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test ExitHook called after non-Unit method - original return value preserved`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.ExitHook

        fun main() {
          val calculator = Calculator()
          val result = calculator.add(3, 4)
          println("result=${'$'}result")
        }

        object CalculatorHook {
          @ExitHook(
            className = "Calculator",
            methodName = "add",
            paramsTypes = "(kotlin.Intkotlin.Int)"
          )
          fun onExitAdd(calc: Calculator, a: Int, b: Int) {
            println("EXIT_HOOK_CALLED")
          }
        }

        class Calculator {
          fun add(a: Int, b: Int): Int = a + b
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("result=7" in output, "Original return value should be preserved. Output: $output")
    assertTrue("EXIT_HOOK_CALLED" in output, "Exit hook should be called. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test ExitHook can access receiver state`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.ExitHook

        fun main() {
          val timer = Timer("operation")
          timer.run()
        }

        object TimerHook {
          @ExitHook(
            className = "Timer",
            methodName = "run",
            paramsTypes = "()"
          )
          fun onExitRun(timer: Timer) {
            println("TIMING:${'$'}{timer.name}")
          }
        }

        class Timer(val name: String) {
          fun run() { println("RUNNING:${'$'}name") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("RUNNING:operation" in output, "Original method should execute. Output: $output")
    assertTrue("TIMING:operation" in output, "Exit hook should access receiver. Output: $output")
    val runIndex = output.indexOf("RUNNING")
    val timingIndex = output.indexOf("TIMING")
    assertTrue(runIndex < timingIndex, "Original runs before exit hook. Output: $output")
  }
}
