package com.zzt.kid.plugin.model

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression

class EntryHookMeta(
  val targetClassName: String,
  val targetMethodName: String,
  val targetMethodParamsTypes: String,
  val ignoreCallSuper: Boolean,
  val entryFunction: IrFunction
) {
  var pass: Boolean = false
  var irReturn: IrExpression? = null
  override fun toString(): String {
    return "EntryHookMeta(targetClassName='$targetClassName', targetMethodName='$targetMethodName', targetMethodParamsTypes='$targetMethodParamsTypes', ignoreCallSuper=$ignoreCallSuper, entryFunction=$entryFunction, pass=$pass, irReturn=$irReturn)"
  }
}
