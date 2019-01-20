package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import com.snobot.simulator.plugin.cpp_sim.RunCppSnobotSimPlugin
import com.snobot.simulator.plugin.cpp_sim.SnobotSimCppRobotPlugin
import com.snobot.simulator.plugin.cpp_sim.SnobotSimCppSetupRule
import com.snobot.simulator.plugin.java_sim.RunJavaSnobotSimPlugin
import com.snobot.simulator.plugin.java_sim.SnobotSimJavaRobotPlugin

import jaci.gradle.toolchains.ToolchainsPlugin


class SnobotSimulatorPlugin implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    void apply(Project project) {
        SnobotSimulatorVersionsExtension snobotSimExtension = project.extensions.create("snobotSim", SnobotSimulatorVersionsExtension, project)

        project.configurations.maybeCreate("snobotSimCompile")
        project.configurations.maybeCreate("snobotSimJavaNative")
        project.configurations.maybeCreate("snobotSimCppNative")

        project.pluginManager.apply(RunJavaSnobotSimPlugin)
        project.pluginManager.apply(RunCppSnobotSimPlugin)

        project.pluginManager.apply(SnobotSimJavaRobotPlugin)

        project.plugins.withType(ToolchainsPlugin).all {
            logger.info("SnobotSim detected C++ Tools, adding plugins")
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