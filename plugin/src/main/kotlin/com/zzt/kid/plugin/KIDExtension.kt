package com.zzt.kid.plugin

import com.zzt.kid.plugin.model.HookMeta
import com.zzt.kid.plugin.transformer.HookTransformer
import com.zzt.kid.plugin.visitor.HookMetaCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

open class KIDExtension(
    private val messageCollector: MessageCollector
): IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val entryHookMetaList = mutableListOf<HookMeta>()
        moduleFragment.acceptChildrenVoid(HookMetaCollector(
            pluginContext,
            entryHookMetaList
        ))
        messageCollector.report(CompilerMessageSeverity.LOGGING, "entryHookMetaList: $entryHookMetaList")
        moduleFragment.transform(HookTransformer(pluginContext, messageCollector), entryHookMetaList)
    }
}