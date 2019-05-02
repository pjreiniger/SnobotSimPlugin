package com.snobot.simulator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class SnobotSimBasePlugin implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String mNativeDir;
    protected final String mConfigurationName;

    SnobotSimBasePlugin(String aNativeDir, String aConfigurationName) {
        mNativeDir = aNativeDir
        mConfigurationName = aConfigurationName
    }

    void applyBase(Project project, Map<String, String> aMavenRepos) {

        aMavenRepos.each { repoPair ->
            project.repositories.maven { repo ->
                repo.name = repoPair.getKey()
                repo.url = repoPair.getValue()
            }
        }
        project.repositories { mavenLocal() }
        project.repositories { mavenCentral() }
    }
}
