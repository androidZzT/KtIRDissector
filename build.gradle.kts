import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.multiplatform) apply(false)
    alias(libs.plugins.kotlin.jvm) apply(false)
    alias(libs.plugins.ksp) apply(false)
}

subprojects {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(host=SonatypeHost.S01, automaticRelease = true)
            signAllPublications()
        }
    }
}