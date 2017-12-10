package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.internal.os.OperatingSystem


class SnobotSimPlugin implements Plugin<Project> {
    void apply(Project project) {
    
        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }
        
        
        def snobotSimVersion = "0.4"
        def jfreecommon = "1.0.16"
        def jfreechart = "1.0.13"
        def log4j = "1.2.16"
        def snakeyaml = "1.18"
        def miglayout = "4.2"
        def ntcore = "+"
        def cscore = "+"
        def opencv = "+"
        
        project.configurations.maybeCreate("snobotSimLibs")
        project.dependencies.add("snobotSimLibs", "com.snobot.simulator:snobot_sim_gui:${snobotSimVersion}:all")
        project.dependencies.add("snobotSimLibs", "jfree:jcommon:${jfreecommon}")
        project.dependencies.add("snobotSimLibs", "jfree:jfreechart:${jfreechart}")
        project.dependencies.add("snobotSimLibs", "log4j:log4j:${log4j}")
        project.dependencies.add("snobotSimLibs", "org.yaml:snakeyaml:${snakeyaml}")
        project.dependencies.add("snobotSimLibs", "com.miglayout:miglayout-swing:${miglayout}")
        project.dependencies.add("snobotSimLibs", "com.miglayout:miglayout-core:${miglayout}")
        project.dependencies.add("snobotSimLibs", "edu.wpi.first.ntcore:ntcore-jni:${ntcore}:all")
        project.dependencies.add("snobotSimLibs", "edu.wpi.first.cscore:cscore-jni:${cscore}:all")
        project.dependencies.add("snobotSimLibs", "org.opencv:opencv-jni:${opencv}:all")
        project.dependencies.add("snobotSimLibs", "net.java.jinput:jinput:2.0.7")
        project.dependencies.add("snobotSimLibs", "net.java.jutils:jutils:1.0.0")
        
        if (OperatingSystem.current().isWindows()) 
        {
            project.dependencies.add("snobotSimLibs", "com.snobot.simulator:snobot_sim_java:${snobotSimVersion}:uber_native-windows")
        }
        else 
        {
            project.dependencies.add("snobotSimLibs", "com.snobot.simulator:snobot_sim_java:${snobotSimVersion}:uber_native-linux")
        }

        // Configuration for pulling in everything needed for SnobotSim
        project.dependencies.ext.snobotSimCompile = {
            def l = [
                project.configurations.getByName("snobotSimLibs").dependencies.each {
                    it
                }
            ]

            l
        }
        
        createSimulateTask(project)
        updateIdePlugins(project)
    }
    
    void createSimulateTask(Project project)
    {
        // Task to run the simulator from the cmd line
        project.task("simulate") { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Launch the simulator"

            task.doLast {
            
                List<String> args = getRunArguments(project)
                println "The command"
                args.each{
                    print it + " "
                }
                println ""


                
                //println args
                //println "DDDDDDDD"
                
                //def config = project.configurations.getByName("wpiTools")
                //Set<File> jarfiles = config.files(config.dependencies.find { d -> d.name == "SmartDashboard" })
                //ProcessBuilder builder
                //if (OperatingSystem.current().isWindows()) {
                //    builder = new ProcessBuilder(
                //        "cmd", "/c", "start",
                //        "java", "-jar", "${jarfiles.first().absolutePath}".toString()
                //    )
                //} else {
                //    builder = new ProcessBuilder(
                //        Jvm.current().getExecutable("java").absolutePath,
                //        "-jar",
                //        jarfiles.first().absolutePath
                //    )
                //}
                //
                //builder.directory(smartDashboardDirectory())
                //builder.start()
            }
        }
    }
    
    void updateIdePlugins(Project project)
    {
        // Update eclipse to correctly use the jinput natives
        if(project.hasProperty("eclipse")) {
            project.eclipse.classpath.file {
                withXml {
                    provider ->
                    provider.asNode().findAll { it.@path.contains("jinput") && !it.@path.contains("natives") }.each {
                            def container = it
                            container.appendNode('attributes').appendNode('attribute', [name: 'org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY', value:project.name + "/build/native_libs"])
                    }
                }
            }
        }
        else {
            println "Eclipse not configured, will not update classpath"
        }
    }
    
    List<String> getRunArguments(Project project)
    {
        def snobotSimLibsConfig = project.configurations.getByName("snobotSimLibs")
        def compileConfig = project.configurations.getByName("compile")
        File simulatorJar = snobotSimLibsConfig.files(snobotSimLibsConfig.dependencies.find { d -> d.name == "snobot_sim_gui" }).first()
        
        // Get Classpath
        def classpath = ""
        classpath = addToClasspath(snobotSimLibsConfig, classpath)
        classpath = addToClasspath(compileConfig, classpath)
        classpath = classpath[ 0..-2 ] // Remove extra semicolon.  Two because there is a space
        
        List<String> args = new ArrayList<>();
        if (OperatingSystem.current().isWindows()) {
            args.add("java")
        }
        
        args.add("-classpath")
        args.add(classpath)
        args.add("com.snobot.simulator.Main")
        
        return args
    }
    
    String addToClasspath(def configurationType, def classpath) {
        configurationType.dependencies.each {
            def the_dep = configurationType.files(it)
            if (!the_dep.isEmpty()) {
              classpath += the_dep.first().toString() + ";"
              println "  " + the_dep.first().toString() + ";"
            }
            else {
                println "  UH OH: " + it.toString()
            }
        }
        
        return classpath
    }

}