package com.zzt.kid.plugin.model

import org.jetbrains.kotlin.ir.declarations.IrFunction

const val HOOK_ANNOTATION_ENTRY = "com.zzt.kid.annotation.EntryHook"
const val HOOK_ANNOTATION_REPLACE = "com.zzt.kid.annotation.Replace"
const val METHOD_HOOK = "com.zzt.kid.runtime.MethodHook"

class HookMeta(
  val targetClassName: String,
  val targetMethodName: String,
  val targetMethodParamsTypes: String,
  val function: IrFunction,
  val ignoreCallSuper: Boolean,
) {
  var hookType: HookType = HookType.UNKNOWN

  override fun toString(): String {
    return "HookMeta(" +
      "targetClassName='$targetClassName', " +
      "targetMethodName='$targetMethodName'," +
      " targetMethodParamsTypes='$targetMethodParamsTypes'," +
      " function=$function, " +
      "ignoreCallSuper=$ignoreCallSuper, " +
      "hookType=$hookType" +
      ")"
  }
}

enum class HookType {
  ENTRY,
  EXIT,
  REPLACE,
  RETURN,
  UNKNOWN
}
