package com.snobot.simulator.plugin.cpp_sim

import org.gradle.api.tasks.TaskAction

import com.snobot.simulator.plugin.BaseExtractSnobotSimNatives

import groovy.transform.CompileStatic

@CompileStatic
class ExtractSnobotSimCppNatives extends BaseExtractSnobotSimNatives {

    private final String configurationName;
    private final String outputDirectory;

    public ExtractSnobotSimCppNatives() {
        super("snobotSimCppNative", "tmp/snobotSimCppNative")
    }

    @TaskAction
    void extract() {
        runExtract()
    }
}