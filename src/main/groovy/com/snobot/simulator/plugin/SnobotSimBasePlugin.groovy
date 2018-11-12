package com.snobot.simulator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.internal.os.OperatingSystem

import edu.wpi.first.gradlerio.wpi.WPIExtension

class SnobotSimBasePlugin implements Plugin<Project> {

    void apply(Project project) {


        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }

        project.repositories { mavenLocal() }
        project.repositories { mavenCentral() }

        def wpilibExt = project.extensions.getByType(WPIExtension)
        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)
        setupSnobotSimBaseDeps(project, snobotSimExt, wpilibExt)
        extractLibs(project, "snobotSimBaseNative", getExtractedPath())
    }


    void setupSnobotSimBaseDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

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

        project.configurations
        { snobotSimBaseNative }

        project.dependencies
        {
            snobotSimBaseNative "net.java.jinput:jinput:${snobotSimExt.jinput}"
            snobotSimBaseNative "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimBaseNative "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}:${nativeSnobotSimClassifier}"
            snobotSimBaseNative "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}:native-${nativeSnobotSimClassifier}"

            // Not done with GradleRIO
            snobotSimBaseNative "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpiExt.wpilibVersion}:${nativeclassifier}@zip"
        }

        project.dependencies.ext.snobotSimBase = {
            [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_utilities:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}",
                "jfree:jfreechart:${snobotSimExt.jfreechart}",
                "org.apache.logging.log4j:log4j-core:${snobotSimExt.log4j}",
                "org.yaml:snakeyaml:${snobotSimExt.snakeyaml}",
                "com.miglayout:miglayout-swing:${snobotSimExt.miglayout}",
                "net.java.jinput:jinput:${snobotSimExt.jinput}",
            ]
        }
    }

    void extractLibs(Project project, String configurationName, String outputDirectory, deleteOldFolder=false) {
        def nativeZips = project.configurations.getByName(configurationName)

        project.task("snobotSimUnzip${configurationName}", type: Zip, dependsOn: nativeZips) { Task task ->

            FileCollection extractedFiles = null as FileCollection
            nativeZips.dependencies
                    .matching { true}
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

            def File dir = new File(project.buildDir, outputDirectory)
            if (deleteOldFolder && dir.exists())
            {
                dir.deleteDir()
            }

            if(!dir.exists())
            {
                dir.parentFile.mkdirs()
            }

            project.copy { CopySpec s ->
                s.from(project.files { extractedFiles.files })
                s.into(dir)
            }
        }
    }

    static String getExtractedPath()
    {
        return "tmp/jniExtractDir";
    }
}