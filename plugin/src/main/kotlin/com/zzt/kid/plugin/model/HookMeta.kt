package com.zzt.kid.plugin.model

import org.jetbrains.kotlin.ir.declarations.IrFunction

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
