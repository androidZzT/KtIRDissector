# Kotlin IR Dissector(K.I.D)
<img src="project_logo.png" width="300px" alt="DALL.E 生成">

<a href="https://github.com/androidZzT/KtIRDissector/blob/main/README.md">简体中文</a> | <a href="https://github.com/androidZzT/KtIRDissector/blob/main/README_EN.md">English</a>

Kotlin IR Dissector(K.I.D)是一个用于编译期 transform Kotlin IR 的工具。你可以在项目中直接编写 kotlin 代码来 hook 目标方法入口\出口或完全替换目标方法。
此工具基于 Kotlin Compiler Plugin 实现。

## 安装

settings.gradle 中添加仓库源
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

工程中依赖插件
```kotlin
plugins {
  id("io.github.androidzzt.kid-plugin") version <latest_version>
}
```

KMP(Kotlin Multiplatform) 工程中依赖注解库
```kotlin
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("io.github.androidzzt.kid:kid-annotation:<latest_version>")
      }
    }
  }
}
```

Kotlin Android(JVM) 工程中依赖注解库
```kotlin
dependencies {
  implementation("io.github.androidzzt.kid:kid-annotation:<latest_version>")
}
```

## 功能

### 1. Hook 目标方法入口

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class EntryHook(
    val className: String, // 目标方法所在类的全限定名
    val methodName: String, // 目标方法名
    val paramsTypes: String, // 目标方法参数类型列表，以逗号分隔
    val ignoreSuper: Boolean = false // 是否忽略调用 super
)
```

比如，现在有 `com.example.Logger#log` 方法，定义如下：
```kotlin
package com.example

class Logger {
  val tag = "Logger"
  fun log(msg: String) {
    println(msg)
  }
}
```
我们想要 hook log 方法的入口，可以这样写：
```kotlin
import com.example.Logger
import com.zzt.kid.annotation.EntryHook
import com.zzt.kid.runtime.MethodHook

object LoggerEntryHook {
  @EntryHook(
    className = "com.example.Logger",
    methodName = "log",
    paramsTypes = "(kotlin.String)" // 注意，这里的参数类型需要加上括号，如果有多个参数，以逗号分隔
  )
  fun hookLogEntry(caller: Logger, msg: String): MethodHook<Unit> {
    println("entry: ${caller.tag}:: msg=$msg")
    return MethodHook.intercept() // 返回 MethodHook.intercept() 表示拦截目标方法，不再执行
  }
}
```

上面代码中有一个类 `MethodHook`, 定义如下：
```kotlin
class MethodHook<T>(val ret: T? = null) {
  var pass: Boolean = ret == null

  companion object {
    fun <T> pass() = MethodHook<T>(null).apply { pass = true }
    fun <T> intercept(ret: T? = null) = MethodHook(ret)
  }
}
```
- 泛型 T 是目标方法的返回类型，如果目标方法无返回类型，那么 T 就是 Unit
- `MethodHook.pass()` 表示继续执行目标方法
- `MethodHook.intercept()` 表示拦截目标方法，不再执行，如果目标方法有返回值，可以通过 `MethodHook.intercept(ret)` 来指定返回值

经过 LogEntryHook 的处理后，Logger#log 方法的实现变成了这样：
```kotlin
package com.example

class Logger {
  val tag = "Logger"
  fun log(msg: String) {
    val methodHook = LoggerEntryHook.hookLogEntry(this, msg)
    if (methodHook.pass) {
      println(msg)
    }
  }
}
```

### 2. 完全替换目标方法

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Replace(
  val className: String, // 目标方法所在类的全限定名
  val methodName: String, // 目标方法名
  val paramsTypes: String, // 目标方法参数类型列表，以逗号分隔
  val ignoreSuper: Boolean = false // 是否忽略调用 super
)
```

比如，我们认为 log 方法内部可能出现异常，我们想替换 log 方法，让它在出现异常时打印异常信息，可以这样写：
```kotlin
object LoggerHook { 
    @Replace(
        className = "com.example.Logger",
        methodName = "log",
        paramsTypes = "(kotlin.String)"
    )
    fun replaceLog(caller: Logger, msg: String) {
        try {
          println(msg)
        } catch (e: Exception) {
          e.printStackTrace()
        }
    }
}
```
注意，@Replace 不需要配合 MethodHook 使用，插件会将目标方法的实现完全替换成注解方法的实现。

