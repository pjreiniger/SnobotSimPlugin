package com.snobot.simulator.plugin;

import org.gradle.api.Project;

import groovy.transform.CompileStatic


@CompileStatic
class SnobotSimulatorVersionsExtension {

    String snobotSimVersion = "2019-1.2.0"
    String snobotSimCtreVersion = "5.12.0_V1_RC"
    String snobotSimRevVersion = "1.1.9_V0"
    String jfreechart = "1.0.13"
    String jinput = "2.0.9"
    String log4j = "2.11.0"
    String snakeyaml = "1.18"
    String miglayout = "4.2"

    final Project project

    SnobotSimulatorVersionsExtension(Project project) {
        this.project = project
    }

    Map<String, String> versions() {
        return [
            "snobotSimVersion" : snobotSimVersion,
            "snobotSimCtreVersion" : snobotSimCtreVersion,
            "jfreechart" : jfreechart,
            "jinput" : jinput,
            "log4j" : log4j,
            "snakeyaml" : snakeyaml,
            "miglayout" : miglayout,
        ]
    }
}
