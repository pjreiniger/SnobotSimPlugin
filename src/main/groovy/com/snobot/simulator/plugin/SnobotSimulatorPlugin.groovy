package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task

import com.snobot.simulator.plugin.cpp_sim.SnobotSimCppRobotPlugin
import com.snobot.simulator.plugin.cpp_sim.SnobotSimCppSetupRule
import com.snobot.simulator.plugin.java_sim.SnobotSimJavaRobotPlugin

import jaci.gradle.toolchains.ToolchainsPlugin


class SnobotSimulatorPlugin implements Plugin<Project> {

    void apply(Project project) {
        SnobotSimulatorVersionsExtension snobotSimExtension = project.extensions.create("snobotSim", SnobotSimulatorVersionsExtension, project)

        project.pluginManager.apply(RunJavaSnobotSimPlugin)
        project.pluginManager.apply(RunCppSnobotSimPlugin)

        project.pluginManager.apply(SnobotSimJavaRobotPlugin)

        project.plugins.withType(ToolchainsPlugin).all {
            project.pluginManager.apply(SnobotSimCppSetupRule)
            project.pluginManager.apply(SnobotSimCppRobotPlugin)
        }


        project.task("snobotSimVersions") { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Print all versions of the snobotSim block"
            task.doLast {
                snobotSimExtension.versions().each { String key, String v ->
                    println key.padRight(21) + ": " + v
                }
            }
        }
    }
}