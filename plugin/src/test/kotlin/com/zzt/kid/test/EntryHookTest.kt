package com.zzt.kid.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.plugin.KIDComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class EntryHookTest {

  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `test EntryHook`() {
    val result = compile(
      sourceFiles = listOf(
        SourceFile.fromPath(File("src/main/kotlin/com/zzt/kid/annotation/EntryHook.kt")),
        SourceFile.fromPath(File("src/test/kotlin/com/zzt/kid/test/Logger.kt")),
        SourceFile.fromPath(File("src/test/kotlin/com/zzt/kid/test/LoggerHook.kt")),
        SourceFile.fromPath(File("src/test/kotlin/com/zzt/kid/test/main.kt"))
      ),
      plugin = KIDComponentRegistrar(true),
    )
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    val ktClazz = result.classLoader.loadClass("com.zzt.kid.test.MainKt")
    val main = ktClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
    main.invoke(null)
  }
}