package com.zzt.kid.test

import com.zzt.kid.annotation.EntryHook
import com.zzt.kid.compile.MethodHook

fun main() {
  val logger = Logger()
  val ret = logger.log("Hello World")
  println("ret= $ret")
}

class LoggerHook {
  @EntryHook(
    className = "com.zzt.kid.test.Logger",
    methodName = "log",
    paramsTypes = "(kotlin.String)",
    ignoreSuper = false
  )
  fun hookLogEntry(): MethodHook<Boolean> {
    val a = 1
    val b = 2
    val c = a + b
    println("before log $c")
    if (c == 3) {
      return MethodHook.intercept(false)
    } else {
      return MethodHook.pass()
    }
  }
}

class Logger {
  fun log(msg: String) : Boolean {
    println(msg)
    return true;
  }
}