package com.snobot.simulator.plugin;

import org.gradle.api.Project;

import groovy.transform.CompileStatic


@CompileStatic
class SnobotSimulatorVersionsExtension {

    String snobotSimVersion = "2018-2.0.0"
    String snobotSimCtreVersion = "V0_5.1.2.1"
    String jfreecommon = "1.0.16"
    String jfreechart = "1.0.13"
    String log4j = "2.11.0"
    String snakeyaml = "1.18"
    String miglayout = "4.2"
    String wpilib = "+"
    String wpiutil = "+"
    String ntcore = "+"
    String cscore = "+"
    String opencv = "+"

    final Project project

    SnobotSimulatorVersionsExtension(Project project) {
        this.project = project

        def versions = this.versions()
        //def versions = new JsonSlurper().parseText(versions_str)[year] as Map
        this.versions().forEach { String property, String v ->
            this.setProperty(property, (versions as Map)[v] ?: this.getProperty(property))
        }
    }

    Map<String, String> versions() {
        return [
            "snobotSimVersion" : snobotSimVersion,
            "jfreecommon" : jfreecommon,
            "jfreechart" : jfreechart,
            "log4j" : log4j,
            "snakeyaml" : snakeyaml,
            "miglayout" : miglayout,
            "ntcore" : ntcore,
            "cscore" : cscore,
            "opencv" : opencv,
        ]
    }
}
