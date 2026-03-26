// To run this sample:
// 1. From the project root, publish to mavenLocal: ./gradlew publishToMavenLocal
// 2. Uncomment include(":sample") in settings.gradle.kts
// 3. Run: ./gradlew :sample:run

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.androidzzt.kid-plugin")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.androidzzt:kid-annotation:1.0.0")
}

application {
    mainClass.set("com.zzt.kid.sample.MainKt")
}
