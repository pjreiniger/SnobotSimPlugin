package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;


class SnobotSimPluginExtension {

}



class SnobotSimPlugin implements Plugin<Project> {
    void apply(Project project) {
    
        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }
        
        
        def snobotSimVersion = "0.3"
        def jfreecommon = "1.0.16"
        def jfreechart = "1.0.13"
        def log4j = "1.2.16"
        def snakeyaml = "1.18"
        def miglayout = "4.2"
        def ntcore = "+"
        def cscore = "+"
        def opencv = "+"
        

        project.dependencies.ext.snobotSimCompile = {
            def l = [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_utilites:${snobotSimVersion}",
                "com.snobot.simulator:snobot_sim_java:${snobotSimVersion}:uber_native-linux",
                "com.snobot.simulator:snobot_sim_java:${snobotSimVersion}:uber_native-windows",
            ]

            l
        }

        project.dependencies.ext.snobotSimRuntime = {
            def l = [
                "jfree:jcommon:${jfreecommon}",
                "jfree:jfreechart:${jfreechart}",
                "log4j:log4j:${log4j}",
                "org.yaml:snakeyaml:${snakeyaml}",
                "com.miglayout:miglayout-swing:${miglayout}",
                "edu.wpi.first.ntcore:ntcore-jni:${ntcore}:all",
                "edu.wpi.first.cscore:cscore-jni:${cscore}:all",
                "org.opencv:opencv-jni:${opencv}:all",
            ]

            l
        }
        
        project.task('hello') {
            doLast {
                println 'Hello from the GreetingPlugin'
            }
        }
    }
}