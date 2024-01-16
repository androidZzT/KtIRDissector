package com.zzt.kid.plugin.visitor

import com.zzt.kid.plugin.model.HOOK_ANNOTATION_ENTRY
import com.zzt.kid.plugin.model.HOOK_ANNOTATION_REPLACE
import com.zzt.kid.plugin.model.HookMeta
import com.zzt.kid.plugin.model.HookType
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class HookMetaCollector(
  private val context: IrPluginContext,
  private val metaList: MutableList<HookMeta>
): IrElementVisitorVoid {

  override fun visitModuleFragment(declaration: IrModuleFragment) {
    declaration.acceptChildren(this, null)
  }
  override fun visitFile(declaration: IrFile) {
    //TODO 插件参数指定范围，只处理指定包下的文件，增加编译速度
    declaration.acceptChildren(this, null)
  }

  override fun visitClass(declaration: IrClass) {
    declaration.acceptChildren(this, null)
  }

  override fun visitFunction(declaration: IrFunction) {
    if (declaration.body == null) {
      super.visitFunction(declaration)
      return
    }

    val hookMetaList = declaration.annotations.map {
      when (it.type.render()) {
        HOOK_ANNOTATION_ENTRY -> {
          val meta = newHookMetaByAnnotation(declaration, it)
          meta.hookType = HookType.ENTRY
          meta
        }
        HOOK_ANNOTATION_REPLACE -> {
          val meta = newHookMetaByAnnotation(declaration, it)
          meta.hookType = HookType.REPLACE
          meta
        }
        else -> null
      }
    }
    metaList.addAll(hookMetaList.filterNotNull())
  }

  private fun newHookMetaByAnnotation(irFunction: IrFunction, anno: IrConstructorCall): HookMeta {
    val className = anno.getValueArgument(0)?.let {
      (it as? IrConst<*>)?.value as? String
    } ?: ""
    val methodName = anno.getValueArgument(1)?.let {
      (it as? IrConst<*>)?.value as? String
    } ?: ""
    val paramsTypes = anno.getValueArgument(2)?.let {
      (it as? IrConst<*>)?.value as? String
    } ?: ""
    val ignoreSuper = anno.getValueArgument(3)?.let {
      (it as? IrConst<*>)?.value as? Boolean
    } ?: false

    return HookMeta(
      targetClassName = className,
      targetMethodName = methodName,
      targetMethodParamsTypes = paramsTypes,
      ignoreCallSuper = ignoreSuper,
      function = irFunction,
    )
  }
}