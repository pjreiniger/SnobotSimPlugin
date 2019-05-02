package com.snobot.simulator.plugin.java_sim;

import org.gradle.api.Project

import com.snobot.simulator.plugin.JsonDependencyParser
import com.snobot.simulator.plugin.SnobotSimBasePlugin

import edu.wpi.first.gradlerio.wpi.WPIExtension

public class SnobotSimJavaRobotPlugin extends SnobotSimBasePlugin {

    SnobotSimJavaRobotPlugin() {
        super("tmp/snobotSimJava", "snobotSimJavaNative");
    }

    void apply(Project project) {

        def wpilibExt = project.extensions.getByType(WPIExtension)

        JsonDependencyParser parser = new JsonDependencyParser();
        parser.loadSnobotSimConfig(wpilibExt.deps);

        super.applyBase(project, parser.mMavenRepos);
        setupSnobotSimJavaDeps(project, parser.mJavaLibraries, parser.mJniLibraries)
    }

    void setupSnobotSimJavaDeps(Project project, List<String> aJavaDependencies, List<String> aNativeDependencies) {

        project.dependencies.ext.snobotSimJavaNative = { aNativeDependencies }

        project.dependencies.ext.snobotSimJava = {
            def output = new ArrayList<>(aJavaDependencies)
            project.dependencies.ext.snobotSimJavaNative().each {
                project.dependencies.add("snobotSimJavaNative", it)
            }

            output
        }
    }
}
