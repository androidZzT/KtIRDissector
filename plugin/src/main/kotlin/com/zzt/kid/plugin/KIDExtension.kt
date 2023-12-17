package com.zzt.kid.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.resolve.BindingContext

class KIDExtension(
    val string: String,
    val file: String
): IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        TODO("Not yet implemented")
    }
}