package com.snobot.simulator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.tasks.Copy;
import org.gradle.internal.os.OperatingSystem

class SnobotSimSetupPlugin implements Plugin<Project> {

    void apply(Project project) {

        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }

        project.repositories.maven { repo ->
            repo.name = "PublishedMaven"
            repo.url = "https://plugins.gradle.org/m2/"
        }

        project.repositories { mavenLocal() }

        def os_name = getOsName()


        def snobotSimVersionExt = project.extensions.create("snobotSimVersions", SnobotSimulatorVersionsExtension, project)


        project.task("snobotSimVersions") { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Print all versions of the snobotSim block"
            task.doLast {
                snobotSimVersionExt.versions().each { String key, String v ->
                    println "${v} (${key})"
                }
            }
        }


        ////////////////////////////////////////////
        // Unzip jinput
        ////////////////////////////////////////////
        project.configurations
        {
            native3rdPartyDeps
            ctreNativeDeps
        }
        project.dependencies
        {
            native3rdPartyDeps "net.java.jinput:jinput:2.0.7"
            ctreNativeDeps "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}:native-all"
        }

        // Need to only unzip the platform specific one
        project.task("snobotSimUnzipCtreNativeTools", type: Copy, dependsOn: project.configurations.ctreNativeDeps) { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Unzips any CTRE native libraries"

            project.configurations.ctreNativeDeps.each { Object zipFile ->
                project.copy {
                    includeEmptyDirs = false
                    from project.zipTree(zipFile)
                    into "build/native_libs"
                    include "**/*.dll"
                    include "**/*.lib"
                    include "**/*.pdb"
                    include "**/*.so*"
                    include "**/*.a"
                    include "**/*.dylib"

                    eachFile {

                        if(!it.getRelativePath().toString().contains(os_name)) {
                            it.exclude()
                        }

                        it.setPath(it.getRelativePath().toString().replace(os_name + "/x86-64", ""))
                        it.setPath(it.getRelativePath().toString().replace(os_name + "/x86", ""))
                    }
                }
            }
        }

        project.task("snobotSimUnzipNativeTools", type: Copy, dependsOn: project.configurations.native3rdPartyDeps) { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Unzips any 3rd Party native libraries"

            project.configurations.native3rdPartyDeps.each { Object zipFile ->
                project.copy {
                    includeEmptyDirs = false
                    from project.zipTree(zipFile)
                    into "build/native_libs"
                    include "**/*.dll"
                    include "**/*.lib"
                    include "**/*.pdb"
                    include "**/*.so*"
                    include "**/*.a"
                }
            }
        }

        // Configuration for pulling in everything needed for SnobotSim
        project.dependencies.ext.snobotSimCompile = {
            def l = [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_utilities:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:adx_family:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:adx_family:${snobotSimVersionExt.snobotSimVersion}:all",
                "com.snobot.simulator:navx_simulator:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:navx_simulator:${snobotSimVersionExt.snobotSimVersion}:all",
                "com.snobot.simulator:wpilib:${snobotSimVersionExt.snobotSimVersion}:all",
                "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}:native-all",
                "com.snobot.simulator:temp_hal_interface:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:temp_hal_interface:${snobotSimVersionExt.snobotSimVersion}:all",
                "jfree:jcommon:${snobotSimVersionExt.jfreecommon}",
                "jfree:jfreechart:${snobotSimVersionExt.jfreechart}",
                "org.apache.logging.log4j:log4j-api:${snobotSimVersionExt.log4j}",
                "org.apache.logging.log4j:log4j-core:${snobotSimVersionExt.log4j}",
                "org.yaml:snakeyaml:${snobotSimVersionExt.snakeyaml}",
                "com.miglayout:miglayout-swing:${snobotSimVersionExt.miglayout}",
                "com.miglayout:miglayout-core:${snobotSimVersionExt.miglayout}",
                "org.opencv:opencv-jni:${snobotSimVersionExt.opencv}:all",
                "net.java.jinput:jinput:2.0.7",
                "net.java.jutils:jutils:1.0.0",
            ]

            l
        }

        project.dependencies.ext.snobotSimJavaCompile = {
            def l = ["edu.wpi.first.ntcore:ntcore-jni:${snobotSimVersionExt.ntcore}:all", "edu.wpi.first.cscore:cscore-jni:${snobotSimVersionExt.cscore}:all",]

            l
        }
    }

    String getOsName()
    {

        def os_name = ""
        if (OperatingSystem.current().isWindows())
        {
            os_name = "windows"
        }
        else
        {
            os_name = "linux"
        }

        return os_name;
    }
}