package com.zzt.kid.plugin.transformer

import com.zzt.kid.compile.MethodHook
import com.zzt.kid.plugin.model.HookMeta
import com.zzt.kid.plugin.model.HookType
import com.zzt.kid.utils.getProperty
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName

class HookTransformer(
  private val pluginContext: IrPluginContext,
): IrElementTransformer<List<HookMeta>> {

  override fun visitFile(declaration: IrFile, data: List<HookMeta>): IrFile {
    return super.visitFile(declaration, data)
  }

  override fun visitClass(declaration: IrClass, data: List<HookMeta>): IrStatement {
    println("visitClass:: ${declaration.packageFqName}.${declaration.name} data: $data")
    return super.visitClass(declaration, data)
  }

  override fun visitFunction(declaration: IrFunction, data: List<HookMeta>): IrStatement {
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
      println("----- transform function start -----")
      val body = declaration.body
      body?.let {b ->
        declaration.body = when (it.hookType) {
          HookType.ENTRY -> {
            entryTransform(declaration, b, it.function)
          }
          HookType.REPLACE -> {
            replaceTransform(declaration, b, it.function)
          }
          else -> {
            throw IllegalStateException("unknown hook type: ${it.hookType}")
          }
        }
      }
    }
    return super.visitFunction(declaration, data)
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun entryTransform(
    originFunction: IrFunction,
    irBody: IrBody,
    function: IrFunction
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, originFunction.symbol).irBlockBody {
      // 获取 MethodHook 类的引用
      val methodHookClass = pluginContext.referenceClass(FqName(MethodHook::class.java.name))?.owner
        ?: throw IllegalStateException("MethodHook class not found")

      if (function.returnType.classFqName != FqName(MethodHook::class.java.name)) {
        throw IllegalStateException("your method must return a MethodHook instance, now return ${function.returnType.classFqName}")
      }

      // 创建对插入 function 的调用
      val result = callInsertFunction(function, originFunction)
      val condition = getMethodHookPassExpression(methodHookClass, result)
      // 创建条件判断
      +irIfThenElse(
        context.irBuiltIns.unitType,
        condition = condition,  // 使用 pass 属性作为条件
        thenPart = irBlock {  // 如果 pass 为 true，执行原始函数体
          for (statement in irBody.statements) {
            +statement
          }
        },
        elsePart = irBlock {  // 如果 pass 为 false，返回 MethodHook ret
          val retProperty = getProperty(irGet(result), methodHookClass.properties.single { it.name.asString() == "ret" })
          +irReturn(retProperty)
        }
      )
    }.also {
      it.dump()
    }
  }

  private fun replaceTransform(
    originFunction: IrFunction,
    irBody: IrBody,
    function: IrFunction
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, originFunction.symbol).irBlockBody {
      val result = callInsertFunction(function, originFunction)
      +irReturn(irGet(result))
    }.also {
      it.dump()
    }
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun IrBlockBodyBuilder.callInsertFunction(
    insertFunction: IrFunction,
    originFunction: IrFunction
  ): IrVariable {
    return irTemporary(getInsertFuncIrCall(insertFunction, originFunction))
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun IrBlockBodyBuilder.getInsertFuncIrCall(
    insertFunction: IrFunction,
    originFunction: IrFunction
  ): IrFunctionAccessExpression {
    return irCall(insertFunction).apply {
      val hookInstance = pluginContext.referenceClass(FqName(insertFunction.parentAsClass.name.asString()))
      hookInstance?.let { // 约定：Hook 代码规定必须使用 Object，调用方法需要传入该实例
        dispatchReceiver = irGetObject(hookInstance)
      }
      // 获取 originFunction 的 dispatchReceiver
      val originDispatchReceiver = originFunction.dispatchReceiverParameter?.let { irGet(it) }
      insertFunction.valueParameters.forEachIndexed { index, _ ->
        if (index == 0) {// 约定：原始方法的实例作为第一个参数
          putValueArgument(index, originDispatchReceiver)
        } else {
          if (index - 1 < originFunction.valueParameters.size) { // 约定：原始方法的参数作为后续参数
            putValueArgument(index, irGet(originFunction.valueParameters[index - 1]))
          } else {
            //TODO 处理其他加过注解的参数
          }
        }
      }
    }
  }

  private fun IrBlockBodyBuilder.getMethodHookPassExpression(
    methodHookClass: IrClass,
    result: IrVariable
  ): IrExpression {
    // 获取 MethodHook 的 pass 属性
    val passProperty = methodHookClass.properties.single { it.name.asString() == "pass" }
    return getProperty(irGet(result), passProperty)
  }
}