package com.zzt.kid.plugin.visitor

import com.zzt.kid.annotation.EntryHook
import com.zzt.kid.plugin.model.EntryHookMeta
import com.zzt.kid.plugin.transformer.EntryHookTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class EntryHookCollector(
  private val context: IrPluginContext,
  private val metaList: MutableList<EntryHookMeta>
): IrElementVisitorVoid {

  override fun visitModuleFragment(declaration: IrModuleFragment) {
    declaration.acceptChildren(this, null)
  }
  override fun visitFile(declaration: IrFile) {
    declaration.acceptChildren(this, null)
  }

  override fun visitClass(declaration: IrClass) {
    declaration.acceptChildren(this, null)
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction) {
    if (declaration.body == null) {
      super.visitSimpleFunction(declaration)
      return
    }

    // 1. 判断是否被 EntryHook 注解标注
    val entryHookAnnotation = declaration.annotations.find {
      it.type.render() == EntryHook::class.qualifiedName
    }

    entryHookAnnotation?.let { annotation ->
      // 2. 获取注解参数
      val className = annotation.getValueArgument(0)?.let {
        (it as? IrConst<*>)?.value as? String
      } ?: ""
      val methodName = annotation.getValueArgument(1)?.let {
        (it as? IrConst<*>)?.value as? String
      } ?: ""
      val paramsTypes = annotation.getValueArgument(2)?.let {
        (it as? IrConst<*>)?.value as? String
      } ?: ""
      val ignoreSuper = annotation.getValueArgument(3)?.let {
        (it as? IrConst<*>)?.value as? Boolean
      } ?: false

      // 3. 生成 EntryHookMeta
      val meta = EntryHookMeta(
        targetClassName = className,
        targetMethodName = methodName,
        targetMethodParamsTypes = paramsTypes,
        ignoreCallSuper = ignoreSuper,
        entryFunction = declaration
      )
      metaList.add(meta)
    }
  }
}