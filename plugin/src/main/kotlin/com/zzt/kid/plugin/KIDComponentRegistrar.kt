package com.zzt.kid.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class KIDComponentRegistrar(override val supportsK2: Boolean) : CompilerPluginRegistrar() {

    @OptIn(ExperimentalCompilerApi::class)
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val string = configuration.get(KIDCommandLineProcessor.ARG_STRING, "String")
        val file = configuration.get(KIDCommandLineProcessor.ARG_FILE, "File")
        IrGenerationExtension.registerExtension((KIDExtension(string, file)))
    }

}