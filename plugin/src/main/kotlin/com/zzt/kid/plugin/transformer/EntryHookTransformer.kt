package com.zzt.kid.plugin.transformer

import com.zzt.kid.plugin.model.EntryHookMeta
import com.zzt.kid.utils.costEnter
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName

class EntryHookTransformer(
  private val pluginContext: IrPluginContext,
): IrElementTransformer<List<EntryHookMeta>> {

  override fun visitFile(declaration: IrFile, data: List<EntryHookMeta>): IrFile {
    return super.visitFile(declaration, data)
  }

  override fun visitClass(declaration: IrClass, data: List<EntryHookMeta>): IrStatement {
    println("visitClass:: ${declaration.packageFqName}.${declaration.name} data: $data")
    return super.visitClass(declaration, data)
  }

  override fun visitFunction(declaration: IrFunction, data: List<EntryHookMeta>): IrStatement {
    val funcName = StringBuilder()
    funcName.append(declaration.name)
    funcName.append("(")
    declaration.valueParameters.forEach {
      funcName.append(it.type.classFqName)
    }
    funcName.append(")")
    println("   visitFunction:: funcName: $funcName" )
    data.find { (it.targetMethodName + it.targetMethodParamsTypes).trimIndent() == funcName.toString() }?.let {
      println("   find target function: $funcName")
      println("----- transform function entry -----")
      val body = declaration.body
      body?.let {b ->
        declaration.body = transformFunctionBody(declaration, b, it.entryFunction)
        (declaration.body as IrBlockBody).statements.forEach {
          println("   statement: ${it.render()}")
        }
      }
    }
    return super.visitFunction(declaration, data)
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun transformFunctionBody(
    irFunction: IrFunction,
    irBody: IrBody,
    entryFunction: IrFunction
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, irFunction.symbol).irBlockBody {
      // 创建对 entryFunction 的调用
      +irCall(entryFunction).apply {
        // 设置参数，这里假设 entryFunction 不需要参数，或者你需要传递适当的参数
        // 例如，如果 entryFunction 需要当前函数的参数，你可以这样设置：
        // this.dispatchReceiver = irFunction.dispatchReceiverParameter?.let { irGet(it) }
        // this.extensionReceiver = irFunction.extensionReceiverParameter?.let { irGet(it) }
        // irFunction.valueParameters.forEachIndexed { index, irParameter ->
        //     putValueArgument(index, irGet(irParameter))
        // }
        val hookInstance = pluginContext.referenceClass(FqName(entryFunction.parentAsClass.name.asString()))
        hookInstance?.let { // 约定：Entry 代码规定必须使用 Object，调用方法需要传入该实例
          dispatchReceiver = irGetObject(hookInstance)
        }
        // 获取 originFunction 的 dispatchReceiver
        val originDispatchReceiver = irFunction.dispatchReceiverParameter?.let { irGet(it) }
        entryFunction.valueParameters.forEachIndexed { index, irParameter ->
          if (index == 0) {
            putValueArgument(index, originDispatchReceiver) // 约定：原始方法的实例作为第一个参数
          }
          //TODO 处理其他加过注解的参数
        }
      }

      // 添加原有函数体的所有语句
      for (statement in irBody.statements) {
        +statement
      }
    }
  }
}