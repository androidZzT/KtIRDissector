package com.zzt.kid.plugin.transformer

import com.zzt.kid.plugin.model.EntryHookMeta
import com.zzt.kid.utils.costEnter
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.statements
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
        declaration.body = transformFunctionBody(declaration, b, it.entryIrBody)
        (declaration.body as IrBlockBody).statements.forEach {
          println("   statement: ${it.render()}")
        }
      }
    }
    return super.visitFunction(declaration, data)
  }

  private fun transformFunctionBody(
    irFunction: IrFunction,
    irBody: IrBody,
    entryBody: IrBody
  ): IrBlockBody {
    return DeclarationIrBuilder(pluginContext, irFunction.symbol).irBlockBody {
      for (statement in entryBody.statements) {
        +statement
      }
      for (statement in irBody.statements) {
        +statement
      }
    }
  }
}