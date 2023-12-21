package com.zzt.kid.test

import com.zzt.kid.annotation.EntryHook

object LoggerHook {

  @EntryHook(
    className = "com.zzt.kid.test.Logger",
    methodName = "log",
    paramsTypes = "()",
    ignoreSuper = false
  )
  fun hookLogEntry() {
    println("before log")
  }
}