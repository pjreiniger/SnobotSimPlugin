package com.snobot.simulator.plugin;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class ConfigManagementPlugin implements Plugin<Project> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigManagementPlugin.class);

    @Override
    public void apply(Project project) {

        project.tasks.register("updateSnobotSimConfig", Task) { Task t ->
            t.group = "SnobotSimulator"
            t.description = "This will download and update your SnobotSim.json config file"

            def useLocal = project.hasProperty("useLocalSnobotSimConfig")
            String newText;

            if(useLocal) {
                def local_file = System.getProperty("user.home") + '/.m2/repository/com/snobot/simulator/SnobotSim.json'
                LOGGER.info("Using local copy of the config at '" + local_file + "'");

                newText = new File(local_file).getText()
            }
            else {
                LOGGER.info("Using remote file");

                def remote_url = "https://raw.githubusercontent.com/pjreiniger/maven_repo/master/com/snobot/simulator/SimulatorConfig.json"
                newText = new URL(remote_url).getText()
            }

            File outputFile = new File("output.json")
            outputFile.delete();
            outputFile << newText
        }
    }
}