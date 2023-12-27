package com.zzt.kid.compile


class MethodHook<T>(val ret: T? = null) {
  var pass: Boolean = ret == null

  companion object {
    fun <T> pass() = MethodHook<T>(null).apply { pass = true }
    fun <T> intercept(ret: T? = null) = MethodHook(ret)
  }
}