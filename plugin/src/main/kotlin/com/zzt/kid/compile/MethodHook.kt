package com.zzt.kid.compile


class MethodHook<T> {
  var pass: Boolean
  var ret: T? = null

  private constructor(pass: Boolean) {
    this.pass = pass
  }

  constructor(ret: T?) {
    this.ret = ret
    pass = false
  }

  companion object {
    fun <T> pass(): MethodHook<T> {
      return MethodHook<T>(true)
    }

    fun <T> intercept(): MethodHook<T> {
      return intercept(null)
    }

    fun <T> intercept(ret: T?): MethodHook<T> {
      return MethodHook<T>(ret)
    }
  }
}