package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Project

import com.snobot.simulator.plugin.SnobotSimBasePlugin
import com.snobot.simulator.plugin.SnobotSimulatorVersionsExtension

import edu.wpi.first.gradlerio.wpi.WPIExtension

public class SnobotSimCppRobotPlugin extends SnobotSimBasePlugin {

    SnobotSimCppRobotPlugin() {
        super("tmp/snobotSimCppNative", "snobotSimCppNative");
    }

    void apply(Project project) {
        def wpilibExt = project.extensions.getByType(WPIExtension)
        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)

        setupSnobotSimCppDeps(project, snobotSimExt, wpilibExt)
        super.applyBase(project)
    }

    void setupSnobotSimCppDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

        project.dependencies.ext.snobotSimCppNative = {
            def output = [
                "net.java.jinput:jinput:${snobotSimExt.jinput}",
                "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}:${mNativeClassifer}",
                "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}:${mNativeClassifer}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}:${mNativeClassifer}",
                // Not done with GradleRIO
                "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                // CPP Specific
                "edu.wpi.first.wpilibc:wpilibc-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.ntcore:ntcore-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.cscore:cscore-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.wpiutil:wpiutil-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.hal:hal-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.cameraserver:cameraserver-cpp:${wpiExt.wpilibVersion}:${mNativeClassifer}@zip",
                "edu.wpi.first.thirdparty.frc2019.opencv:opencv-cpp:${wpiExt.opencvVersion}:${mNativeClassifer}@zip",
                "com.snobot.simulator:snobot_sim:${snobotSimExt.snobotSimVersion}:${mNativeClassifer}@zip"
            ]

            output
        }

        project.dependencies.ext.snobotSimCpp = {
            def output = [
                "edu.wpi.first.wpilibj:wpilibj-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.ntcore:ntcore-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.cscore:cscore-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.wpiutil:wpiutil-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.hal:hal-java:${wpiExt.wpilibVersion}",
                "edu.wpi.first.cameraserver:cameraserver-java:${wpiExt.wpilibVersion}",
            ]
            project.dependencies.ext.snobotSimCppNative().each {
                project.dependencies.add("snobotSimCppNative", it)
            }

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
