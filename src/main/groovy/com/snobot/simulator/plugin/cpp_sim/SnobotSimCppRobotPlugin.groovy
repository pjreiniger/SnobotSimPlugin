package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import com.snobot.simulator.plugin.SnobotSimBasePlugin
import com.snobot.simulator.plugin.SnobotSimulatorVersionsExtension

import edu.wpi.first.gradlerio.wpi.WPIExtension
import edu.wpi.first.gradlerio.wpi.dependencies.WPICommonDeps

public class SnobotSimCppRobotPlugin extends SnobotSimBasePlugin {

   SnobotSimCppRobotPlugin() {
       super("tmp/snobotSimCppNative", "snobotSimCppNative");
   }

    void apply(Project project) {
        def wpilibExt = project.extensions.getByType(WPIExtension)
        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)

        project.configurations
        { snobotSimCppNative }
		
        setupSnobotSimCppDeps(project, snobotSimExt, wpilibExt)
	    super.applyBase(project)

        project.pluginManager.apply(WPICommonDeps)
        extractLibs(project, "snobotSimCppNative", mNativeDir)
    }


    void setupSnobotSimCppDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

        def nativeclassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                OperatingSystem.current().isLinux() ? "linuxx86-64" :
                null
                )

        project.dependencies
        {
            snobotSimCppNative "net.java.jinput:jinput:${snobotSimExt.jinput}"
            snobotSimCppNative "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimCppNative "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimCppNative "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}:native-${nativeSnobotSimClassifier}"

            // Not done with GradleRIO
            snobotSimCppNative "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
        }

        project.dependencies {
            snobotSimCppNative "edu.wpi.first.wpilibc:wpilibc-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.ntcore:ntcore-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.cscore:cscore-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.wpiutil:wpiutil-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.hal:hal-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.cameraserver:cameraserver-cpp:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
            snobotSimCppNative "edu.wpi.first.thirdparty.frc2019.opencv:opencv-cpp:${wpiExt.opencvVersion}:${nativeclassifier}@zip"

            snobotSimCppNative "com.snobot.simulator:snobot_sim:${snobotSimExt.snobotSimVersion}:${nativeclassifier}@zip"
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

            project.dependencies.ext.snobotSimBase().each { output << it }

            output
        }
    }
}
