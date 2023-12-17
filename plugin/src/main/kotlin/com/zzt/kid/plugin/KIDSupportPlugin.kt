package com.zzt.kid.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KIDSupportPlugin : KotlinCompilerPluginSupportPlugin {

    private lateinit var project: Project
    private lateinit var pluginId: String
    private lateinit var group: String
    private lateinit var artifactId: String
    private lateinit var version: String

    override fun apply(target: Project) {
        super.apply(target)
        project = target
        pluginId = target.extensions.getByName("kotlin_plugin_id") as String
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(KIDExtension::class.java) as KIDExtension
        return project.provider {
            listOf(
                SubpluginOption(key = "string", value = extension.string),
                SubpluginOption(key = "file", value = extension.file),
            )
        }
    }

    override fun getCompilerPluginId(): String {
        return pluginId
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            group,
            artifactId,
            version
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return project.plugins.hasPlugin(KIDSupportPlugin::class.java)
    }
}