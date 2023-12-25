package com.zzt.kid.plugin.visitor

import com.zzt.kid.plugin.model.EntryHookMeta
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class MethodHookCollector(
  private val pluginContext: IrPluginContext,
  private val irFunction: IrFunction,
  private val hookMeta: EntryHookMeta
): IrElementVisitorVoid {

  override fun visitReturn(expression: IrReturn) {
    println("visitReturn:: ${expression.render()}")
    if (expression.returnTargetSymbol != irFunction.symbol) //只 transform 目标函数
      return super.visitReturn(expression)
  }
}