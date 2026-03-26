package com.zzt.kid.runtime

class MethodHook<T> private constructor(val ret: T?, val pass: Boolean) {

  companion object {
    fun <T> pass() = MethodHook<T>(null, true)
    fun <T> intercept(ret: T? = null) = MethodHook(ret, false)
  }
}