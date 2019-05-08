package com.snobot.simulator.plugin.cpp_sim;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar
import org.gradle.nativeplatform.SharedLibraryBinarySpec
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RunCppSnobotSimPlugin implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    void apply(Project project) {

        File wrapperExtractDir = new File(project.getBuildDir(), "tmp/SnobotSimWrapper");

        project.tasks.register("extractSnobotSimCppJNI", ExtractSnobotSimCppNatives) { ExtractSnobotSimCppNatives t ->
            t.group = "SnobotSimulator"
        }

        project.tasks.create("copyWrapperLibrary", Copy) { Task task ->
            destinationDir = new File(project.buildDir, "/tmp/snobotSimCppNative")

            project.model {
                binaries {
                    withType(SharedLibraryBinarySpec) { binary ->
                        if (binary.component.name == "snobotSimCppWrapper") {
                            dependsOn binary.buildTask

                            if(!wrapperExtractDir.exists()) {
                                logger.info("Wrapper extraction has not been done... adding dependency");
                                binary.buildTask.dependsOn "extractSnobotSimCppWrapperFiles"
                            }

                            from(binary.sharedLibraryFile) { into "." }
                        }
                    }
                }
            }
        }

        project.tasks.create("extractSnobotSimCppWrapperFiles") { Task task ->

            doLast {
                logger.info("Running the extraction task...");
                def javaTxt = getClass().getClassLoader().getResourceAsStream("RobotSimulatorJni.java").text
                def headerTxt = getClass().getClassLoader().getResourceAsStream("SimulatorJniWrapper.h").text
                def jniTxt = getClass().getClassLoader().getResourceAsStream("RobotSimulatorJni.h").text

                if(!wrapperExtractDir.exists()) {
                    wrapperExtractDir.mkdir();
                }

                new File(wrapperExtractDir, "RobotSimulatorJni.java") << javaTxt;
                new File(wrapperExtractDir, "SimulatorJniWrapper.h") << headerTxt;
                new File(wrapperExtractDir, "RobotSimulatorJni.h") << jniTxt;

                String robotName;

                if(project.hasProperty("robotName")) {
                    robotName = project.robotName
                }
                else {
                    robotName = "Robot"
                }

                def simulatorJniText = """            
#include "RobotSimulatorJni.h"
#include "SimulatorJniWrapper.h"
#include "${robotName}.h"
#include <iostream>

static SimulatorJniWrapper<${robotName}>* wrapper = NULL;

JNIEXPORT void JNICALL Java_RobotSimulatorJni_createRobot
(JNIEnv *, jclass)
{
    wrapper = new SimulatorJniWrapper<${robotName}>(std::make_shared<${robotName}>());
}

JNIEXPORT void JNICALL Java_RobotSimulatorJni_startCompetition
(JNIEnv *, jclass)
{
    wrapper->GetRobot()->StartCompetition();
}
"""
                new File(wrapperExtractDir, "RobotSimulatorJni.cpp") << simulatorJniText;
            }
        }

        project.tasks.withType(Jar).all { Jar jarTask ->
            def attr = jarTask.manifest.attributes
            if (jarTask.name.equals("jar")) {
                project.tasks.create("runCppSnobotSim") { Task task ->
                    task.group = "SnobotSimulator"
                    task.description ="Runs the simulator with SnobotSim"
                    task.dependsOn "extractSnobotSimCppJNI"
                    task.dependsOn jarTask

                    if(project.tasks.findByName("snobotSimCppWrapperReleaseSharedLibrary")) {
                        jarTask.dependsOn "snobotSimCppWrapperReleaseSharedLibrary"
                        task.dependsOn "copyWrapperLibrary"
                    }

                    if(project.tasks.findByName("simulatorExtensionJar")) {
                        task.dependsOn "simulatorExtensionJar"
                    }

                    task.doLast {

                        def classpath = jarTask.archivePath.toString() + envDelimiter()
                        classpath = addToClasspath(project.configurations.getByName("compile"), classpath)
                        classpath = addToClasspath(project.configurations.getByName("snobotSimCompile"), classpath)

                        if(project.tasks.findByName("simulatorExtensionJar")) {
                            project.tasks.getByName("simulatorExtensionJar").outputs.files.each {
                                classpath += it.getAbsolutePath() + envDelimiter()
                            }
                        }


                        classpath = classpath[ 0..-2 ] // Remove extra semicolon.  Two because there is a space

                        def ldpath = "build/tmp/snobotSimCppNative" + envDelimiter()

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
