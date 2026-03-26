package com.zzt.kid.test.entryhook

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.test.captureOutput
import com.zzt.kid.test.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntryHookTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook intercept skips original method`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val logger = Logger()
          logger.log("test_message")
        }

        object LoggerHook {
          @EntryHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun hookLog(logger: Logger, msg: String): MethodHook<Unit> {
            println("HOOK_CALLED")
            return MethodHook.intercept()
          }
        }

        class Logger {
          fun log(msg: String) { println("ORIGINAL_CALLED") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("HOOK_CALLED" in output, "Expected hook to be called. Output: $output")
    assertFalse("ORIGINAL_CALLED" in output, "Original method should be skipped. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook pass executes original method`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val logger = Logger()
          logger.log("test_message")
        }

        object LoggerHook {
          @EntryHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun hookLog(logger: Logger, msg: String): MethodHook<Unit> {
            println("HOOK_CALLED")
            return MethodHook.pass()
          }
        }

        class Logger {
          fun log(msg: String) { println("ORIGINAL_CALLED") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("HOOK_CALLED" in output, "Expected hook to be called. Output: $output")
    assertTrue("ORIGINAL_CALLED" in output, "Original method should also execute. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook with multiple parameters`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val calculator = Calculator()
          calculator.add(10, 20)
        }

        object CalculatorHook {
          @EntryHook(
            className = "Calculator",
            methodName = "add",
            paramsTypes = "(kotlin.Intkotlin.Int)"
          )
          fun hookAdd(calc: Calculator, a: Int, b: Int): MethodHook<Unit> {
            println("HOOK_CALLED:${'$'}a:${'$'}b")
            return MethodHook.intercept()
          }
        }

        class Calculator {
          fun add(a: Int, b: Int) { println("ORIGINAL_CALLED:${'$'}{a + b}") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("HOOK_CALLED:10:20" in output, "Hook should receive correct parameters. Output: $output")
    assertFalse("ORIGINAL_CALLED" in output, "Original method should be skipped. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook on object method`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val ret = Logger.log("Hello World")
          println("ret=${'$'}ret")
        }

        object LoggerHook {
          @EntryHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun hookLogEntry(logger: Logger, msg: String): MethodHook<Unit> {
            println("entry: msg=${'$'}msg, logger=${'$'}{logger.name}")
            return MethodHook.intercept()
          }
        }

        object Logger {
          val name = "Logger"
          fun log(msg: String) { println(msg) }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("entry: msg=Hello World" in output, "Hook message should appear. Output: $output")
    assertFalse("Hello World\n" in output || output.endsWith("Hello World"), "Original println should be skipped. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook hook receives correct parameter values`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val service = Service()
          service.greet("Alice")
        }

        object ServiceHook {
          @EntryHook(
            className = "Service",
            methodName = "greet",
            paramsTypes = "(kotlin.String)"
          )
          fun hookGreet(service: Service, name: String): MethodHook<Unit> {
            println("intercepted:${'$'}name")
            return MethodHook.pass()
          }
        }

        class Service {
          fun greet(name: String) { println("hello:${'$'}name") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("intercepted:Alice" in output, "Hook should receive param 'Alice'. Output: $output")
    assertTrue("hello:Alice" in output, "Original should also run with correct param. Output: $output")
  }
}
