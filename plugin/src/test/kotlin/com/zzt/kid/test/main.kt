package com.zzt.kid.test

import com.zzt.kid.annotation.EntryHook

fun main() {
  val logger = Logger()
  logger.log("Hello World")
}

class LoggerHook {
  @EntryHook(
    className = "com.zzt.kid.test.Logger",
    methodName = "log",
    paramsTypes = "(kotlin.String)",
    ignoreSuper = false
  )
  fun hookLogEntry() {
    val a = 1
    val b = 2
    val c = a + b
    println("before log $c")
  }
}

class Logger {
  fun log(msg: String) {
    println(msg)
  }
}