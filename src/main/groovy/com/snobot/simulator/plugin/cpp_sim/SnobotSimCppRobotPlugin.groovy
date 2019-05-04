package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Project
import org.gradle.api.Task

import com.snobot.simulator.plugin.JsonDependencyParser
import com.snobot.simulator.plugin.SnobotSimBasePlugin

import edu.wpi.first.gradlerio.wpi.WPIExtension

public class SnobotSimCppRobotPlugin extends SnobotSimBasePlugin {

    SnobotSimCppRobotPlugin() {
        super("tmp/snobotSimCppNative", "snobotSimCppNative");
    }

    void apply(Project project) {
        def wpilibExt = project.extensions.getByType(WPIExtension)

        JsonDependencyParser parser = new JsonDependencyParser();
        parser.loadSnobotSimConfig(project, wpilibExt.deps);

        super.applyBase(project, parser.mMavenRepos);
        setupSnobotSimCppDeps(project, parser.mCppLibraries, parser.mCppJavaLibraries, parser.mJavaLibraries, parser.mJniLibraries)


        project.task("snobotSimCppVersions") { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Print all versions of the snobotSim block"
            task.doLast {
                System.out.println("C++ Libraries")
                parser.mCppLibraries.each {
                    System.out.println("  " + it)
                }

                System.out.println("C++ Java Libraries (needed for GUI sim)")
                parser.mCppJavaLibraries.each {
                    System.out.println("  " + it)
                }
            }
        }
    }

    void setupSnobotSimCppDeps(Project project, List<String> aCppDependencies, List<String> aCppJavaDependencies, List<String> aJavaDependencies, List<String> aJniDependencies) {

        project.dependencies.ext.snobotSimCppNative = {
            def output = new ArrayList<>(aCppDependencies)
            output.addAll(aJniDependencies)

            output
        }

        project.dependencies.ext.snobotSimCpp = {
            def output = new ArrayList<>(aJavaDependencies)
            output.addAll(aCppJavaDependencies)

            project.dependencies.ext.snobotSimCppNative().each {
                project.dependencies.add("snobotSimCppNative", it)
            }

            output
        }
    }
}
