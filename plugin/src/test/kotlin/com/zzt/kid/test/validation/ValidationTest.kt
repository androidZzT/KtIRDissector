package com.zzt.kid.test.validation

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.test.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook on class (not object) reports error`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val logger = Logger()
          logger.log("test")
        }

        class LoggerHook {
          @EntryHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun hookLog(logger: Logger, msg: String): MethodHook<Unit> {
            return MethodHook.intercept()
          }
        }

        class Logger {
          fun log(msg: String) { println(msg) }
        }
      """.trimIndent())
    )

    // Hook class must be an object, not a regular class
    assertEquals(KotlinCompilation.ExitCode.INTERNAL_ERROR, result.exitCode,
      "Should fail when hook is defined in a class, not an object")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace on class (not object) reports error`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.Replace

        fun main() {
          val logger = Logger()
          logger.log("test")
        }

        class LoggerHook {
          @Replace(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun replaceLog(logger: Logger, msg: String) {
            println("replaced: ${'$'}msg")
          }
        }

        class Logger {
          fun log(msg: String) { println(msg) }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.INTERNAL_ERROR, result.exitCode,
      "Should fail when hook is defined in a class, not an object")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test valid EntryHook compiles successfully`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.runtime.MethodHook

        fun main() {
          val logger = Logger()
          logger.log("test")
        }

        object LoggerHook {
          @EntryHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun hookLog(logger: Logger, msg: String): MethodHook<Unit> {
            return MethodHook.pass()
          }
        }

        class Logger {
          fun log(msg: String) { println(msg) }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode,
      "Valid EntryHook with object should compile successfully")
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test valid ExitHook compiles successfully`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.ExitHook

        fun main() {
          val logger = Logger()
          logger.log("test")
        }

        object LoggerHook {
          @ExitHook(
            className = "Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)"
          )
          fun onExit(logger: Logger, msg: String) {
            println("exited")
          }
        }

        class Logger {
          fun log(msg: String) { println(msg) }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode,
      "Valid ExitHook with object should compile successfully")
  }
}
