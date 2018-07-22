package com.snobot.simulator.plugin.java_sim;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import com.snobot.simulator.plugin.SnobotSimBasePlugin

import edu.wpi.first.gradlerio.wpi.WPIExtension

public class SnobotSimJavaRobotPlugin extends SnobotSimBasePlugin {

    void apply(Project project) {
        super.apply(project);

        def wpilibExt = project.extensions.getByType(WPIExtension)

        setupSnobotSimJavaDeps(project, wpilibExt)
        extractLibs(project, "snobotSimJavaNative", "native_libs_java", false)
    }

    void setupSnobotSimJavaDeps(Project project, WPIExtension wpiExt) {

        def nativeclassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                OperatingSystem.current().isLinux() ? "linuxx86-64" :
                null
                )

        project.configurations
        { snobotSimJavaNative }

        project.dependencies {
            snobotSimJavaNative "edu.wpi.first.wpilibj:wpilibj-jniShared:${wpiExt.wpilibVersion}:${nativeclassifier}"
            snobotSimJavaNative "org.opencv:opencv-jni:${wpiExt.opencvVersion}:${nativeclassifier}"
        }

        project.dependencies.ext.snobotSimJava = {
            def output = [
                "edu.wpi.first.ntcore:ntcore-jni:${wpiExt.ntcoreVersion}:${nativeclassifier}",
                "edu.wpi.first.cscore:cscore-jni:${wpiExt.cscoreVersion}:${nativeclassifier}",
            ]

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
