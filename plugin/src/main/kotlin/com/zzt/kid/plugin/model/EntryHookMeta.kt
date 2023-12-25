package com.zzt.kid.plugin.model

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody

data class EntryHookMeta(
  val targetClassName: String,
  val targetMethodName: String,
  val targetMethodParamsTypes: String,
  val ignoreCallSuper: Boolean,
  val entryFunction: IrFunction
)
