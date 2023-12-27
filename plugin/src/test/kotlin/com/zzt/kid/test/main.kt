package com.zzt.kid.test

import com.zzt.kid.annotation.EntryHook
import com.zzt.kid.annotation.Replace
import com.zzt.kid.compile.MethodHook

fun main() {
  val logger = Logger()
  val ret = logger.log("Hello World")
  println("ret= $ret")
}

class LoggerHook {
  @Replace(
    className = "com.zzt.kid.test.Logger",
    methodName = "log",
    paramsTypes = "(kotlin.String)",
    ignoreSuper = false
  )
  fun replaceLog(msg: String) {
    try {
      println("log in try block")
      println(msg)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

class Logger {
  fun log(msg: String) {
    println(msg)
  }
}