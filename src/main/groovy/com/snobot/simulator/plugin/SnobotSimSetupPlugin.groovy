package com.snobot.simulator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.internal.os.OperatingSystem

class SnobotSimSetupPlugin implements Plugin<Project> {

    void apply(Project project) {

        def nativeclassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                OperatingSystem.current().isLinux() ? "linuxx86-64" :
                null
                )

        def nativeSnobotSimClassifier = (
                OperatingSystem.current().isWindows() ?
                System.getProperty("os.arch") == 'amd64' ? 'windows-x86-64' : 'windows-x86' :
                OperatingSystem.current().isMacOsX() ? "os x" :
                OperatingSystem.current().isLinux() ? "linux" :
                null
                )

        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }

        project.repositories.maven { repo ->
            repo.name = "PublishedMaven"
            repo.url = "https://plugins.gradle.org/m2/"
        }

        project.repositories { mavenLocal() }


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

            native3rdPartyDeps "edu.wpi.first.wpiutil:wpiutil-cpp:${snobotSimVersionExt.wpiutil}:${nativeclassifier}@zip"
            native3rdPartyDeps "edu.wpi.first.hal:hal:${snobotSimVersionExt.wpilib}:${nativeclassifier}@zip"
            native3rdPartyDeps "edu.wpi.first.halsim:halsim-adx_gyro_accelerometer:${snobotSimVersionExt.wpilib}:${nativeclassifier}@zip"
            native3rdPartyDeps "edu.wpi.first.wpilibj:wpilibj-jniShared:${snobotSimVersionExt.wpilib}:${nativeclassifier}"

            native3rdPartyDeps "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}:native-${nativeSnobotSimClassifier}"
            native3rdPartyDeps "com.snobot.simulator:adx_family:${snobotSimVersionExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            native3rdPartyDeps "com.snobot.simulator:navx_simulator:${snobotSimVersionExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            native3rdPartyDeps "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}:native-${nativeSnobotSimClassifier}"
            native3rdPartyDeps "com.snobot.simulator:temp_hal_interface:${snobotSimVersionExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
        }

        project.task("snobotSimUnzipNativeTools", type: Copy, dependsOn: project.configurations.native3rdPartyDeps) { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Unzips any 3rd Party native libraries"

            FileCollection extractedFiles = null as FileCollection
            def nativeZips = project.configurations.native3rdPartyDeps

            nativeZips.dependencies
                    .matching { Dependency dep -> dep != null && nativeZips.files(dep).size() > 0 }
                    .all { Dependency dep ->
                        nativeZips.files(dep).each { single_dep ->
                            def ziptree = project.zipTree(single_dep)
                            ["**/*.so*", "**/*.so", "**/*.dll", "**/*.dylib"].collect { String pattern ->
                                def fc = ziptree.matching { PatternFilterable pat -> pat.include(pattern) }
                                if (extractedFiles == null) extractedFiles = fc
                                else extractedFiles += fc
                            }
                        }
                    }

            File dir = new File(project.buildDir, "native_libs")
            if (dir.exists()) dir.deleteDir()
            dir.parentFile.mkdirs()

            project.copy { CopySpec s ->
                s.from(project.files { extractedFiles.files })
                s.into(dir)
            }
        }

        // Configuration for pulling in everything needed for SnobotSim
        project.dependencies.ext.snobotSimCompile = {
            def l = [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_utilities:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:adx_family:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:navx_simulator:${snobotSimVersionExt.snobotSimVersion}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimVersionExt.snobotSimCtreVersion}",
                "com.snobot.simulator:temp_hal_interface:${snobotSimVersionExt.snobotSimVersion}",
                "jfree:jcommon:${snobotSimVersionExt.jfreecommon}",
                "jfree:jfreechart:${snobotSimVersionExt.jfreechart}",
                "org.apache.logging.log4j:log4j-api:${snobotSimVersionExt.log4j}",
                "org.apache.logging.log4j:log4j-core:${snobotSimVersionExt.log4j}",
                "org.yaml:snakeyaml:${snobotSimVersionExt.snakeyaml}",
                "com.miglayout:miglayout-swing:${snobotSimVersionExt.miglayout}",
                "com.miglayout:miglayout-core:${snobotSimVersionExt.miglayout}",
                "org.opencv:opencv-jni:${snobotSimVersionExt.opencv}:${nativeclassifier}",
                "net.java.jinput:jinput:2.0.7",
                "net.java.jutils:jutils:1.0.0",
            ]

            l
        }

        project.dependencies.ext.snobotSimJavaCompile = {
            def l = [
                "edu.wpi.first.ntcore:ntcore-jni:${snobotSimVersionExt.ntcore}:${nativeclassifier}",
                "edu.wpi.first.cscore:cscore-jni:${snobotSimVersionExt.cscore}:${nativeclassifier}",
            ]

            l
        }
    }
}