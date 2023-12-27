package com.zzt.kid.test.entryhook

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.test.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ReplaceTest {
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace return Unit`() {

    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        
        import com.zzt.kid.annotation.Replace

        fun main() {
          val logger = Logger()
          val ret = logger.log("Hello World")
          println("ret= ${'$'}ret")
        }

        object LoggerHook {
          @Replace(
            className = "com.zzt.kid.test.Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun replaceLog(caller: Logger, msg: String) {
              try {
                println("log in try block")
              } catch (e: Exception) {
                e.printStackTrace()
              }
          }
        }

        class Logger {
          
          val name = "Logger"
//          private val private_name = "private"
        
          fun log(msg: String) {
            println(msg)
          }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val ktClazz = result.classLoader.loadClass("MainKt")
    val main = ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
    main.invoke(null)
  }

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test Replace return Boolean`() {
    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        
        import com.zzt.kid.annotation.Replace

        fun main() {
          val logger = Logger()
          val ret = logger.log("Hello World")
          println("ret= ${'$'}ret")
        }

        object LoggerHook {
          @Replace(
            className = "com.zzt.kid.test.Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun replaceLog(caller: Logger, msg: String): Boolean {
              try {
                println("log in try block msg=${'$'}msg")
              } catch (e: Exception) {
                e.printStackTrace()
                return false
              }
              return true
          }
        }

        class Logger {
          
          val name = "Logger"
//          private val private_name = "private"
        
          fun log(msg: String): Boolean {
            println(msg)
          }
        }
      """.trimIndent())
    )

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    val ktClazz = result.classLoader.loadClass("MainKt")
    val main = ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
    main.invoke(null)
  }
}