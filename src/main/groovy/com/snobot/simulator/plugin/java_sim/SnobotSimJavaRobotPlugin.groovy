package com.snobot.simulator.plugin.java_sim;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

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

        project.configurations
        { snobotSimJavaNative }
		
        setupSnobotSimJavaDeps(project, snobotSimExt, wpilibExt)
        super.applyBase(project);
        extractLibs(project, "snobotSimJavaNative", mNativeDir)
    }

    void setupSnobotSimJavaDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

        def nativeclassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                OperatingSystem.current().isLinux() ? "linuxx86-64" :
                null
                )

        project.dependencies {
            snobotSimJavaNative "edu.wpi.first.wpiutil:wpiutil-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimJavaNative "edu.wpi.first.hal:hal-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
			
			snobotSimJavaNative "net.java.jinput:jinput:${snobotSimExt.jinput}"
            snobotSimJavaNative "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimJavaNative "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimJavaNative "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}:native-${nativeSnobotSimClassifier}"

            // Not done with GradleRIO
            snobotSimJavaNative "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
        }

        project.dependencies.ext.snobotSimJava = {
            def output = [
                "edu.wpi.first.ntcore:ntcore-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
                "edu.wpi.first.cscore:cscore-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
                "edu.wpi.first.hal:hal-jni:${wpiExt.wpilibVersion}:${nativeclassifier}",
            ]

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
