package com.snobot.simulator.plugin.java_sim;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RunJavaSnobotSimPlugin implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    void apply(Project project) {

        project.tasks.register("extractSnobotSimJavaJNI", ExtractSnobotSimJavaNatives) { ExtractSnobotSimJavaNatives t ->
            t.group = "SnobotSimulator"
        }


        if(project.sourceSets.findByName("simulatorExtensions") != null) {
            project.tasks.register("simulatorExtensionJavaJar", Jar) {
                baseName = "SnobotSimExtensions"
                from project.sourceSets.simulatorExtensions.output
            }
        }


        project.tasks.withType(Test).configureEach { Test t ->
            t.dependsOn("extractSnobotSimJavaJNI")
        }

        project.tasks.withType(Jar).all { Jar jarTask ->
            def attr = jarTask.manifest.attributes
            if (jarTask.name.equals("jar")) {
                project.tasks.create("runJavaSnobotSim") { Task task ->
                    task.group = "SnobotSimulator"
                    task.description ="Runs the simulator with SnobotSim"

                    task.dependsOn "extractSnobotSimJavaJNI"
                    jarTask.dependsOn "extractSnobotSimJavaJNI"

                    task.dependsOn jarTask

                    if(project.tasks.findByName("simulatorExtensionJavaJar")) {
                        task.dependsOn "simulatorExtensionJavaJar"
                    }

                    task.doLast {

                        def classpath = jarTask.archivePath.toString() + envDelimiter()
                        classpath = addToClasspath(project.configurations.getByName("compile"), classpath)
                        classpath = addToClasspath(project.configurations.getByName("snobotSimCompile"), classpath)

                        if(project.tasks.findByName("simulatorExtensionJavaJar")) {
                            project.tasks.getByName("simulatorExtensionJavaJar").outputs.files.each {
                                classpath += it.getAbsolutePath() + envDelimiter()
                                logger.info("Adding custom extension to the classpath: " + it.getAbsolutePath());
                            }
                        }


                        classpath = classpath[ 0..-2 ] // Remove extra semicolon.  Two because there is a space

                        def ldpath = "build/tmp/snobotSimJava" + envDelimiter()

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
                classpath += depChild.toString() + envDelimiter()
                logger.info("Adding dependency to classpath: " + depChild);
            }
        }

        return classpath
    }

}
