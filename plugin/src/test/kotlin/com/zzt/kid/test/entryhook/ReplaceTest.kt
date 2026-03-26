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

class ReplaceTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace return Unit - replacement called original skipped`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val logger = Logger()
          logger.log("Hello World")
        }

        object LoggerHook {
          @Replace(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun replaceLog(caller: Logger, msg: String) {
            println("REPLACEMENT_CALLED:${'$'}msg")
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
    assertTrue("REPLACEMENT_CALLED:Hello World" in output, "Replacement should be called. Output: $output")
    assertFalse("ORIGINAL_CALLED" in output, "Original method should not be called. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace return Boolean - replacement return value used`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val logger = Logger()
          val ret = logger.check("Hello World")
          println("ret=${'$'}ret")
        }

        object LoggerHook {
          @Replace(
            className = "Logger",
            methodName = "check",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun replaceCheck(caller: Logger, msg: String): Boolean {
            println("REPLACEMENT_CALLED")
            return false
          }
        }

        class Logger {
          fun check(msg: String): Boolean {
            println("ORIGINAL_CALLED")
            return true
          }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("REPLACEMENT_CALLED" in output, "Replacement should be called. Output: $output")
    assertTrue("ret=false" in output, "Should use replacement return value false. Output: $output")
    assertFalse("ORIGINAL_CALLED" in output, "Original method should not be called. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace with multiple parameters`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val formatter = Formatter()
          val result = formatter.format("Hello", 42)
          println("result=${'$'}result")
        }

        object FormatterHook {
          @Replace(
            className = "Formatter",
            methodName = "format",
            paramsTypes = "(kotlin.Stringkotlin.Int)"
          )
          fun replaceFormat(formatter: Formatter, msg: String, count: Int): String {
            return "REPLACED:${'$'}msg:${'$'}count"
          }
        }

        class Formatter {
          fun format(msg: String, count: Int): String {
            return "ORIGINAL:${'$'}msg:${'$'}count"
          }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("result=REPLACED:Hello:42" in output, "Replacement should receive correct params. Output: $output")
    assertFalse("ORIGINAL" in output, "Original method should not be called. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace replacement can access receiver`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val counter = Counter("myCounter")
          counter.increment(5)
        }

        object CounterHook {
          @Replace(
            className = "Counter",
            methodName = "increment",
            paramsTypes = "(kotlin.Int)"
          )
          fun replaceIncrement(counter: Counter, amount: Int) {
            println("REPLACE:${'$'}{counter.name}:${'$'}amount")
          }
        }

        class Counter(val name: String) {
          fun increment(amount: Int) { println("ORIGINAL:${'$'}name:${'$'}amount") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("REPLACE:myCounter:5" in output, "Replacement should access receiver. Output: $output")
    assertFalse("ORIGINAL" in output, "Original method should not be called. Output: $output")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace with no additional parameters`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val service = Service()
          service.run()
        }

        object ServiceHook {
          @Replace(
            className = "Service",
            methodName = "run",
            paramsTypes = "()"
          )
          fun replaceRun(service: Service) {
            println("REPLACEMENT_RUN")
          }
        }

        class Service {
          fun run() { println("ORIGINAL_RUN") }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val output = captureOutput {
      val ktClazz = result.classLoader.loadClass("MainKt")
      ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }.invoke(null)
    }
    assertTrue("REPLACEMENT_RUN" in output, "Replacement should be called. Output: $output")
    assertFalse("ORIGINAL_RUN" in output, "Original method should not be called. Output: $output")
  }
}
