package com.zzt.kid.plugin

import com.zzt.kid.plugin.model.EntryHookMeta
import com.zzt.kid.plugin.transformer.EntryHookTransformer
import com.zzt.kid.plugin.visitor.EntryHookCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.BindingContext

class KIDExtension(
    val string: String,
    val file: String
): IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val entryHookMetaList = mutableListOf<EntryHookMeta>()
        println("----- collect entry hook meta -----")
        moduleFragment.acceptChildrenVoid(EntryHookCollector(
            pluginContext,
            entryHookMetaList
        ))
        println("----- entry hook meta list: $entryHookMetaList -----")

        println("----- transform entry hook -----")
        moduleFragment.files.forEach {irFile ->
            entryHookMetaList.find { irFile.packageFqName == FqName(it.targetClassName) }?.let {
                irFile.transform(EntryHookTransformer(pluginContext), it)
            }
        }
    }
}