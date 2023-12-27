package com.zzt.kid.plugin

import com.zzt.kid.plugin.model.HookMeta
import com.zzt.kid.plugin.transformer.HookTransformer
import com.zzt.kid.plugin.visitor.HookMetaCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class KIDExtension(
    val string: String,
    val file: String
): IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val entryHookMetaList = mutableListOf<HookMeta>()
        moduleFragment.acceptChildrenVoid(HookMetaCollector(
            pluginContext,
            entryHookMetaList
        ))
        moduleFragment.transform(HookTransformer(pluginContext), entryHookMetaList)
    }
}