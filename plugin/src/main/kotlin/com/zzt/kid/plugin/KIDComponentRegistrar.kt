package com.zzt.kid.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KIDComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        messageCollector.report(CompilerMessageSeverity.LOGGING, "KIDComponentRegistrar:: registerExtensions")
        val string = configuration.get(KIDCommandLineProcessor.ARG_STRING, "String")
        val file = configuration.get(KIDCommandLineProcessor.ARG_FILE, "File")
        IrGenerationExtension.registerExtension((KIDExtension(messageCollector)))
    }

}