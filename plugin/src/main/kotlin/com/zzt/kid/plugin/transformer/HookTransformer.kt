package com.zzt.kid.plugin.transformer

import com.zzt.kid.plugin.model.HookMeta
import com.zzt.kid.plugin.model.HookType
import com.zzt.kid.plugin.model.METHOD_HOOK
import com.zzt.kid.utils.getProperty
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
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
  private val messageCollector: MessageCollector
): IrElementTransformer<List<HookMeta>> {

  override fun visitFile(declaration: IrFile, data: List<HookMeta>): IrFile {
    return super.visitFile(declaration, data)
  }

  override fun visitClass(declaration: IrClass, data: List<HookMeta>): IrStatement {
    return super.visitClass(declaration, data)
  }

  override fun visitFunction(declaration: IrFunction, data: List<HookMeta>): IrStatement {
    val funcName = buildString {
      append(declaration.name)
      append("(")
      declaration.valueParameters.forEach { append(it.type.classFqName) }
      append(")")
    }
    messageCollector.report(CompilerMessageSeverity.LOGGING, "visiting function: $funcName")

    data.find { (it.targetMethodName + it.targetMethodParamsTypes).trimIndent() == funcName }?.let {
      messageCollector.report(CompilerMessageSeverity.LOGGING, "applying ${it.hookType} hook to: $funcName")
      val body = declaration.body
      body?.let { b ->
        declaration.body = when (it.hookType) {
          HookType.ENTRY -> entryTransform(declaration, b, it.function)
          HookType.EXIT  -> exitTransform(declaration, b, it.function)
          HookType.REPLACE -> replaceTransform(declaration, b, it.function)
          else -> {
            messageCollector.report(
              CompilerMessageSeverity.ERROR,
              "KID: Unsupported hook type '${it.hookType}' on function '$funcName'. " +
              "Supported types: @EntryHook, @ExitHook, @Replace."
            )
            b
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
    hookFunction: IrFunction
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, originFunction.symbol).irBlockBody {
      val methodHookClass = pluginContext.referenceClass(FqName(METHOD_HOOK))?.owner
        ?: error("KID: MethodHook class not found on classpath. Ensure the annotation module is a dependency.")
      val result = callInsertFunction(hookFunction, originFunction)
      val condition = getMethodHookPassExpression(methodHookClass, result)
      +irIfThenElse(
        context.irBuiltIns.unitType,
        condition = condition,
        thenPart = irBlock {
          for (statement in irBody.statements) { +statement }
        },
        elsePart = irBlock {
          val retProperty = getProperty(irGet(result), methodHookClass.properties.single { it.name.asString() == "ret" })
          +irReturn(retProperty)
        }
      )
    }
  }

  private fun replaceTransform(
    originFunction: IrFunction,
    @Suppress("UNUSED_PARAMETER") irBody: IrBody,
    hookFunction: IrFunction
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, originFunction.symbol).irBlockBody {
      val result = callInsertFunction(hookFunction, originFunction)
      +irReturn(irGet(result))
    }
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun exitTransform(
    originFunction: IrFunction,
    irBody: IrBody,
    exitFunction: IrFunction
  ): IrBlockBody {
    // Unit-returning functions in Kotlin IR have no explicit IrReturn in top-level statements.
    // Non-unit functions have an explicit IrReturn as the last statement.
    return DeclarationIrBuilder(pluginContext, originFunction.symbol).irBlockBody {
      var foundReturn = false
      for (stmt in irBody.statements) {
        if (stmt is IrReturn) {
          foundReturn = true
          // Evaluate original return expression first (executes side effects like println)
          val exitResult = irTemporary(stmt.value, nameHint = "exitResult")
          // Call exit hook after original code has run
          callInsertFunction(exitFunction, originFunction)
          // Return the stored original value
          +irReturn(irGet(exitResult))
        } else {
          +stmt
        }
      }
      if (!foundReturn) {
        // Unit function: no explicit IrReturn — add hook at end, function returns Unit implicitly.
        callInsertFunction(exitFunction, originFunction)
      }
    }
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun IrBlockBodyBuilder.callInsertFunction(
    insertFunction: IrFunction,
    originFunction: IrFunction
  ): IrVariable {
    return irTemporary(
      value = getInsertFuncIrCall(insertFunction, originFunction),
      nameHint = "returnValue"
    )
  }

  @OptIn(FirIncompatiblePluginAPI::class)
  private fun IrBlockBodyBuilder.getInsertFuncIrCall(
    insertFunction: IrFunction,
    originFunction: IrFunction
  ): IrCall {
    val hookClassRef = pluginContext.referenceClass(insertFunction.parent.fqNameForIrSerialization)
      ?: error(
        "KID: Hook class '${insertFunction.parent.fqNameForIrSerialization}' not found. " +
        "Ensure the hook method is defined inside an object declaration."
      )

    if (hookClassRef.owner.kind != ClassKind.OBJECT) {
      error(
        "KID: Hook class '${hookClassRef.owner.name}' must be an 'object' declaration, not a class. " +
        "Change 'class ${hookClassRef.owner.name}' to 'object ${hookClassRef.owner.name}'."
      )
    }

    return irCall(insertFunction.symbol).also {
      it.dispatchReceiver = irGetObject(hookClassRef)
      insertFunction.valueParameters.forEachIndexed { index, _ ->
        if (index == 0) {
          it.putValueArgument(index, originFunction.dispatchReceiverParameter?.let { param -> irGet(param) })
        } else if (index - 1 < originFunction.valueParameters.size) {
          it.putValueArgument(index, irGet(originFunction.valueParameters[index - 1]))
        }
      }
    } as IrCall
  }

  private fun IrBlockBodyBuilder.getMethodHookPassExpression(
    methodHookClass: IrClass,
    result: IrVariable
  ): IrExpression {
    val passProperty = methodHookClass.properties.single { it.name.asString() == "pass" }
    return getProperty(irGet(result), passProperty)
  }
}
