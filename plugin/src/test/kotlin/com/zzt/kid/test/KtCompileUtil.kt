package com.zzt.kid.test

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.zzt.kid.plugin.KIDComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
fun compile(
  sourceFiles: List<SourceFile>,
  plugin: CompilerPluginRegistrar = KIDComponentRegistrar(),
): JvmCompilationResult {
  return KotlinCompilation().apply {
    sources = sourceFiles
    compilerPluginRegistrars = listOf(plugin)
    inheritClassPath = true
  }.compile()
}

@OptIn(ExperimentalCompilerApi::class)
fun compile(
  sourceFile: SourceFile,
  plugin: CompilerPluginRegistrar = KIDComponentRegistrar(),
): JvmCompilationResult {
  return compile(listOf(sourceFile), plugin)
}