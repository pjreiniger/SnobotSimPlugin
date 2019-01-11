package com.snobot.simulator.plugin.java_sim

import org.gradle.api.tasks.TaskAction

import com.snobot.simulator.plugin.BaseExtractSnobotSimNatives

import groovy.transform.CompileStatic

@CompileStatic
class ExtractSnobotSimJavaNatives extends BaseExtractSnobotSimNatives {

    private final String configurationName;
    private final String outputDirectory;

    public ExtractSnobotSimJavaNatives() {
        super("snobotSimJavaNative", "tmp/snobotSimJava")
    }

    @TaskAction
    void extract() {
        runExtract()
    }
}