package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;


class SnobotSimulatorPlugin implements Plugin<Project> 
{

    void apply(Project project) {
    
        project.pluginManager.apply(SnobotSimSetupPlugin)
        project.pluginManager.apply(RunSnobotSimPlugin)
    }

    
}