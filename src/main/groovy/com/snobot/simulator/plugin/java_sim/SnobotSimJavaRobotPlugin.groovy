package com.snobot.simulator.plugin.java_sim;

import org.gradle.api.Project

import com.snobot.simulator.plugin.SnobotSimBasePlugin
import com.snobot.simulator.plugin.SnobotSimulatorVersionsExtension

import edu.wpi.first.gradlerio.wpi.WPIExtension

public class SnobotSimJavaRobotPlugin extends SnobotSimBasePlugin {

    SnobotSimJavaRobotPlugin() {
        super("tmp/snobotSimJava", "snobotSimJavaNative");
    }

    void apply(Project project) {

        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)
        def wpilibExt = project.extensions.getByType(WPIExtension)

        setupSnobotSimJavaDeps(project, snobotSimExt, wpilibExt)
        super.applyBase(project);
    }

    void setupSnobotSimJavaDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

        project.dependencies.ext.snobotSimJavaNative = {
            def output = [
                "edu.wpi.first.wpiutil:wpiutil-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip",
                "edu.wpi.first.hal:hal-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip",
                "net.java.jinput:jinput:${snobotSimExt.jinput}",
                "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}:${nativeclassifier}",
                "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}:${nativeclassifier}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}:${nativeclassifier}",
                // Not done with GradleRIO
                "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpiExt.wpilibVersion}:${nativeclassifier}@zip",
            ]

            output
        }

        project.dependencies.ext.snobotSimJava = {
            def output = [
                "edu.wpi.first.ntcore:ntcore-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
                "edu.wpi.first.cscore:cscore-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
                "edu.wpi.first.hal:hal-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
                //                "edu.wpi.first.thirdparty.frc2019.opencv:opencv-jni:${wpiExt.opencvVersion}:${nativeclassifier}",
            ]
            project.dependencies.ext.snobotSimJavaNative().each {
                project.dependencies.add("snobotSimJavaNative", it)
            }

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
