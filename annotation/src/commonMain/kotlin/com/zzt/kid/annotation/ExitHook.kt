package com.zzt.kid.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ExitHook(
    val className: String,
    val methodName: String,
    val paramsTypes: String,
    val ignoreSuper: Boolean = false
)
