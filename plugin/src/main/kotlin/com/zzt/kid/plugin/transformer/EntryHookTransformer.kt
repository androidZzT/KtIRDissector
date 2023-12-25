package com.zzt.kid.plugin.transformer

import com.zzt.kid.compile.MethodHook
import com.zzt.kid.plugin.model.EntryHookMeta
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
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
//    println("   visitFunction:: funcName: $funcName" )
    data.find { (it.targetMethodName + it.targetMethodParamsTypes).trimIndent() == funcName.toString() }?.let {
      println("   find target function: $funcName")
      println("----- transform function entry -----")
      val body = declaration.body
      body?.let {b ->
        declaration.body = transformFunctionBody(declaration, b, it.entryFunction)
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
      if (entryFunction.returnType.classFqName != FqName(MethodHook::class.java.name)) {
        throw IllegalStateException("your method must return a MethodHook instance, now return ${entryFunction.returnType.classFqName}")
      }

      // 创建对 entryFunction 的调用
      val result = irTemporary(irCall(entryFunction).apply {
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
      })

      // 获取 MethodHook 类的引用
      val methodHookClass = pluginContext.referenceClass(FqName("com.zzt.kid.compile.MethodHook"))?.owner
        ?: throw IllegalStateException("MethodHook class not found")
      // 获取 MethodHook 的 pass 属性
      val passProperty = methodHookClass.properties.single { it.name.asString() == "pass" }
      // 创建条件表达式
      val condition = getProperty(irGet(result), passProperty)
      println("   condition: ${condition.render()}")

      // 创建条件判断
      +irIfThenElse(
        context.irBuiltIns.unitType,
        condition = condition,  // 使用 pass 属性作为条件
        thenPart = irBlock {  // 如果 pass 为 true，执行原始函数体
          for (statement in irBody.statements) {
            +statement
          }
        },
        elsePart = irBlock {  // 如果 pass 为 false，不执行任何操作或执行替代逻辑
          // 可以添加替代逻辑或留空
        }
      )
    }
  }

  private fun IrBlockBodyBuilder.getProperty(receiver: IrExpression, property: IrProperty): IrExpression {
    val getter = property.getter ?: throw IllegalStateException("Property ${property.name} has no getter")
    return irCall(getter).apply {
      dispatchReceiver = receiver
    }
  }
}