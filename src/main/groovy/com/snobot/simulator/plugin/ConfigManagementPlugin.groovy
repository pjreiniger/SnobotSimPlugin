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

                def remote_url;

                if(project.hasProperty("test_remote_url")) {
                    LOGGER.info("Using a custom remote url: '" + project.test_remote_url + "'");
                    remote_url = project.test_remote_url
                }
                else {
                    remote_url = "https://raw.githubusercontent.com/snobotsim/maven_repo/master/release/"
                }
                remote_url += "com/snobot/simulator/SimulatorConfig.json"
                LOGGER.info("Using file '" + remote_url + "'");
                newText = new URL(remote_url).getText()
                LOGGER.info(newText);
            }

            File outputFile = new File("snobotsim/SnobotSim.json")
            outputFile.delete();
            outputFile << newText
        }
    }
}