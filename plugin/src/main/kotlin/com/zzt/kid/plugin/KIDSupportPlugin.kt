package com.zzt.kid.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KIDSupportPlugin : KotlinCompilerPluginSupportPlugin {

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
            "1.0.8"
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.project.plugins.hasPlugin(KIDSupportPlugin::class.java)
    }
}