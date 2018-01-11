package com.snobot.simulator.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.tasks.Copy;
import org.gradle.internal.os.OperatingSystem
import groovy.transform.CompileStatic

@CompileStatic
class SnobotSimulatorVersionsExtension {

    String snobotSimVersion = "0.7.1"
    String jfreecommon = "1.0.16"
    String jfreechart = "1.0.13"
    String log4j = "1.2.16"
    String snakeyaml = "1.18"
    String miglayout = "4.2"
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



class SnobotSimulatorPlugin implements Plugin<Project> {
    void apply(Project project) {
    
        project.repositories.maven { repo ->
            repo.name = "SnobotSim"
            repo.url = "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/"
        }
    
        project.repositories.maven { repo ->
            repo.name = "PublishedMaven"
            repo.url = "https://plugins.gradle.org/m2/"
        }
        
        def os_name = ""
        if (OperatingSystem.current().isWindows()) 
        {
            os_name = "windows"
        }
        else
        {
            os_name = "linux"
        }
        
        
        def snobotSimVersionExt = project.extensions.create("snobotSimVersions", SnobotSimulatorVersionsExtension, project)
            
        
        project.task("snobotSimVersions") { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Print all versions of the snobotSim block"
            task.doLast {
                snobotSimVersionExt.versions().each { String key, String v ->
                    println "${v} (${key})"
                }
            }
        }
        
        
        ////////////////////////////////////////////
        // Unzip jinput
        ////////////////////////////////////////////
        project.configurations
        {
            native3rdPartyDeps
        }
        project.dependencies 
        {
            native3rdPartyDeps "net.java.jinput:jinput:2.0.7"
            native3rdPartyDeps "com.snobot.simulator:ctre_override:${snobotSimVersionExt.snobotSimVersion}:native-" + os_name
        }
        project.task("snobotSimUnzipNativeTools", type: Copy, dependsOn: project.configurations.native3rdPartyDeps) { Task task ->
            task.group = "SnobotSimulator"
            task.description = "Unzips any 3rd Party native libraries"
            
            project.configurations.native3rdPartyDeps.each { Object xxx ->
                project.copy {
                    includeEmptyDirs = false
                    from project.zipTree(xxx)
                    into "build/native_libs"
                    include "**/*.dll"
                    include "**/*.lib"
                    include "**/*.pdb"
                    include "**/*.so*"
                    include "**/*.a"
                }
            }
        }

        // Configuration for pulling in everything needed for SnobotSim
        project.dependencies.ext.snobotSimCompile = {
            def l = [
                "com.snobot.simulator:snobot_sim_gui:${snobotSimVersionExt.snobotSimVersion}:all",
                "jfree:jcommon:${snobotSimVersionExt.jfreecommon}",
                "jfree:jfreechart:${snobotSimVersionExt.jfreechart}",
                "log4j:log4j:${snobotSimVersionExt.log4j}",
                "org.yaml:snakeyaml:${snobotSimVersionExt.snakeyaml}",
                "com.miglayout:miglayout-swing:${snobotSimVersionExt.miglayout}",
                "com.miglayout:miglayout-core:${snobotSimVersionExt.miglayout}",
                "org.opencv:opencv-jni:${snobotSimVersionExt.opencv}:all",
                "net.java.jinput:jinput:2.0.7",
                "net.java.jutils:jutils:1.0.0",
            ]
            
            l += "com.snobot.simulator:snobot_sim_java:${snobotSimVersionExt.snobotSimVersion}:uber_native-" + os_name

            l
        }
        
        project.dependencies.ext.snobotSimJavaCompile = {
            def l = [
                "edu.wpi.first.ntcore:ntcore-jni:${snobotSimVersionExt.ntcore}:all",
                "edu.wpi.first.cscore:cscore-jni:${snobotSimVersionExt.cscore}:all",
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