package com.zzt.kid.plugin.transformer

import com.zzt.kid.plugin.model.EntryHookMeta
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

class FunctionEntryTransformer(
  private val context: IrPluginContext,
  private val irFunction: IrFunction
) : IrElementTransformer<EntryHookMeta> {

}
