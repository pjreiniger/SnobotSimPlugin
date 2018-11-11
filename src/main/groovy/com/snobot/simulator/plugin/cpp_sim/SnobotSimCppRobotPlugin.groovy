package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import com.snobot.simulator.plugin.SnobotSimBasePlugin
import com.snobot.simulator.plugin.SnobotSimulatorVersionsExtension

import edu.wpi.first.gradlerio.wpi.WPIExtension
import edu.wpi.first.gradlerio.wpi.dependencies.WPICommonDeps

public class SnobotSimCppRobotPlugin extends SnobotSimBasePlugin {

    void apply(Project project) {
        project.pluginManager.apply(WPICommonDeps)

        def wpilibExt = project.extensions.getByType(WPIExtension)

        setupSnobotSimCppDeps(project, wpilibExt)
        extractLibs(project, "snobotSimCppNative", "native_libs_cpp")
    }


    void setupSnobotSimCppDeps(Project project, WPIExtension wpiExt) {

        def nativeclassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                OperatingSystem.current().isLinux() ? "linuxx86-64" :
                null
                )

        project.configurations
        { snobotSimCppNative }

        project.dependencies {
            snobotSimCppNative "edu.wpi.first.wpilibc:wpilibc-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.ntcore:ntcore-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.cscore:cscore-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
        }

        project.dependencies.ext.snobotSimCpp = {
            def output = [
                "edu.wpi.first.wpilibj:wpilibj-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.ntcore:ntcore-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.wpiutil:wpiutil-java:${wpiExt.wpilibVersion}",
            ]

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
