pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "KtIRDissector"
include(":plugin")
include(":annotation")
// Note: :sample depends on the published plugin. Run `./gradlew publishToMavenLocal` first,
// then uncomment the line below to build the sample.
// include(":sample")



