package com.snobot.simulator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wpi.first.gradlerio.wpi.WPIExtension

abstract class SnobotSimBasePlugin implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String mNativeDir;
    protected String mConfigurationName;

    protected def nativeclassifier = (
    OperatingSystem.current().isWindows() ?
    System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
    OperatingSystem.current().isMacOsX() ? "osxx86-64" :
    OperatingSystem.current().isLinux() ? "linuxx86-64" :
    null
    )


    SnobotSimBasePlugin(String aNativeDir, String aConfigurationName) {
        mNativeDir = aNativeDir
        mConfigurationName = aConfigurationName
    }

    void applyBase(Project project) {
        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }
        project.repositories.maven { repo ->
            repo.name = "Wpi"
            repo.url = "http://first.wpi.edu/FRC/roborio/maven/release/"
        }
        project.repositories { mavenLocal() }
        project.repositories { mavenCentral() }

        def wpilibExt = project.extensions.getByType(WPIExtension)
        def snobotSimExt = project.extensions.getByType(SnobotSimulatorVersionsExtension)
        setupSnobotSimBaseDeps(project, snobotSimExt, wpilibExt)
    }


    void setupSnobotSimBaseDeps(Project project, SnobotSimulatorVersionsExtension snobotSimExt, WPIExtension wpiExt) {

        project.dependencies.ext.snobotSimBase = {
            [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_utilities:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_joysticks:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}",
                "com.snobot.simulator:ctre_sim_override:${snobotSimExt.snobotSimCtreVersion}",
                "com.snobot.simulator:rev_simulator:${snobotSimExt.snobotSimRevVersion}",
                "jfree:jfreechart:${snobotSimExt.jfreechart}",
                "org.apache.logging.log4j:log4j-core:${snobotSimExt.log4j}",
                "org.yaml:snakeyaml:${snobotSimExt.snakeyaml}",
                "com.miglayout:miglayout-swing:${snobotSimExt.miglayout}",
                "net.java.jinput:jinput:${snobotSimExt.jinput}",
            ]
        }
    }
}
