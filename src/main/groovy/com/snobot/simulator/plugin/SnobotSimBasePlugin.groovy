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

        //        def wpilibExt = project.extensions.getByType(WPIExtension)
        //        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)
        //        setupSnobotSimBaseDeps(project, snobotSimExt, wpilibExt)
    }


    //    void setupSnobotSimBaseDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {
    //
    //        project.dependencies.ext.snobotSimBase = {
    //            [
    //                "com.snobot.simulator:snobot_sim_gui:${snobotSimExt.snobotSimVersion}",
    //                "com.snobot.simulator:snobot_sim_java:${snobotSimExt.snobotSimVersion}",
    //                "com.snobot.simulator:snobot_sim_utilities:${snobotSimExt.snobotSimVersion}",
    //                "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}",
    //                "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}",
    //                "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}",
    //                "com.snobot.simulator:rev_simulator:${snobotSimExt.snobotSimRevVersion}",
    //                "jfree:jfreechart:${snobotSimExt.jfreechart}",
    //                "org.apache.logging.log4j:log4j-core:${snobotSimExt.log4j}",
    //                "org.yaml:snakeyaml:${snobotSimExt.snakeyaml}",
    //                "com.miglayout:miglayout-swing:${snobotSimExt.miglayout}",
    //                "net.java.jinput:jinput:${snobotSimExt.jinput}",
    //            ]
    //        }
    //    }
}
