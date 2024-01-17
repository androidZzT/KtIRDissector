plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildconfig)
}

dependencies {
    implementation(gradleApi())

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.gradle.plugin.api)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.autoService)
    ksp(libs.autoService.ksp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.zacswears.test)
}

gradlePlugin {
    plugins {
        create("KIDPlugin") {
            id = "io.github.androidzzt.kid-plugin"
            implementationClass = "com.zzt.kid.plugin.KIDSupportPlugin"
            displayName = "Kotlin IR Transform Plugin"
            description = "a tool for transforming Kotlin IR at compile time. You can write kotlin code directly in the project to hook the entry and exit of the target method or completely replace the target method."
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

val VERSION_NAME: String by rootProject
buildConfig {
    buildConfigField("String", "VERSION", "\"$VERSION_NAME\"")
}
