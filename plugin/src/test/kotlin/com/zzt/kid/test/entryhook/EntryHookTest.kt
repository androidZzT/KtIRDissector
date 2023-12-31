package com.zzt.kid.test.entryhook

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.test.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EntryHookTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook`() {

    val result = compile(
      sourceFile = SourceFile.kotlin("main.kt", """
        import com.zzt.kid.annotation.EntryHook
        import com.zzt.kid.compile.MethodHook

        fun main() {
          val logger = Logger()
          val ret = logger.log("Hello World")
          println("ret= ${'$'}ret")
        }

        object LoggerHook {
          @EntryHook(
            className = "com.zzt.kid.test.Logger",
            methodName = "log",
            paramsTypes = "(kotlin.String)",
            ignoreSuper = false
          )
          fun hookLogEntry(caller: Logger): MethodHook<Boolean> {
            val a = 1
            val b = 2
            val c = a + b
            println("before log ${'$'}c")
            println("caller.name= ${'$'}{caller.name}")
//            println("caller.private_name= ${'$'}{caller.private_name}")
            if (c == 3) {
              return MethodHook.intercept(false)
            } else {
              return MethodHook.pass()
            }
          }
        }

        class Logger {
          
          val name = "Logger"
//          private val private_name = "private"
        
          fun log(msg: String): Boolean {
            println(msg)
            return true
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