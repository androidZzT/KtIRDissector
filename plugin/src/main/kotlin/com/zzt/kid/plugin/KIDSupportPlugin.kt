package com.zzt.kid.plugin

import KtIRDissector.plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KIDSupportPlugin : KotlinCompilerPluginSupportPlugin {

    private val pluginVersion = BuildConfig.VERSION

    init {
        println("KIDSupportPlugin:: init:: pluginVersion=$pluginVersion")
    }

    override fun apply(target: Project) {
        super.apply(target)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            emptyList()
        }
    }

    override fun getCompilerPluginId(): String {
        return "io.github.androidzzt.kid-plugin"
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            "io.github.androidzzt",
            "kid-plugin",
            pluginVersion
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.project.plugins.hasPlugin(KIDSupportPlugin::class.java)
    }
}