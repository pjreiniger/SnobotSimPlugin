package com.snobot.simulator.plugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar

import jaci.gradle.toolchains.ToolchainsPlugin

class RunSnobotSimPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.tasks.withType(Jar).all { Jar jarTask ->
            def attr = jarTask.manifest.attributes
            if (jarTask.name.equals("jar")) {
                project.tasks.create("runSnobotSim") { Task task ->
                    task.group = "SnobotSimulator"
                    task.description ="Runs the simulator with SnobotSim"
                    task.dependsOn jarTask

                    if(project.tasks.findByName("simulatorExtensionJar")) {
                        task.dependsOn "simulatorExtensionJar"
                    }

                    task.doLast {

                        def classpath = jarTask.archivePath.toString() + ";"
                        classpath = addToClasspath(project.configurations.getByName("compile"), classpath)
                        classpath = addToClasspath(project.configurations.getByName("snobotSimCompile"), classpath)

                        if(project.tasks.findByName("simulatorExtensionJar")) {
                            project.tasks.getByName("simulatorExtensionJar").outputs.files.each {
                                classpath += it.getAbsolutePath() + ";"
                            }
                        }


                        classpath = classpath[ 0..-2 ] // Remove extra semicolon.  Two because there is a space

                        def ldpath = "build/native_libs" + envDelimiter()

                        project.plugins.withType(ToolchainsPlugin).all {
                            ldpath += "build/native_libs_cpp" + envDelimiter()
                        }

                        List<String> args = new ArrayList<>();
                        args.add(OperatingSystem.current().isWindows() ? "java" : Jvm.current().getExecutable("java").absolutePath)
                        args.add("-Djava.library.path=${ldpath}")
                        args.add("-classpath")
                        args.add(classpath)
                        args.add("com.snobot.simulator.Main")

                        project.exec { commandLine args }
                    }
                }
            }
        }
    }

    static String envDelimiter() {
        return OperatingSystem.current().isWindows() ? ";" : ":"
    }

    private String addToClasspath(def configurationType, def classpath) {
        configurationType.dependencies.each {
            def the_dep = configurationType.files(it)
            the_dep.each { depChild ->
                classpath += depChild.toString() + ";"
            }
        }

        return classpath
    }

}