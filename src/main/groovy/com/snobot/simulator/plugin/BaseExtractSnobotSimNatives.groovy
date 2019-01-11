package com.snobot.simulator.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.jvm.tasks.Jar

import groovy.transform.CompileStatic

@CompileStatic
abstract class BaseExtractSnobotSimNatives extends DefaultTask {
    @Internal
    Jar jar

    private final String configurationName;
    private final String outputDirectory;

    public BaseExtractSnobotSimNatives(String configurationName, String outputDirectory) {
        this.configurationName = configurationName
        this.outputDirectory = outputDirectory
    }

    void runExtract() {
        logger.info("Running extraction for configuration: " + configurationName)
        def nativeZips = project.configurations.getByName(configurationName)
        FileCollection extractedFiles = null as FileCollection

        nativeZips.dependencies
                .matching { true}
                .all { Dependency dep ->
                    nativeZips.files(dep).each { single_dep ->
                        def ziptree = project.zipTree(single_dep)
                        logger.info("Extracting native " + single_dep)
                        ["**/*.so*", "**/*.so", "**/*.dll", "**/*.dylib"].collect { String pattern ->
                            def fc = ziptree.matching { PatternFilterable pat -> pat.include(pattern) }
                            if (extractedFiles == null) extractedFiles = fc
                            else extractedFiles += fc
                        }
                    }
                }

        def File dir = new File(project.buildDir, outputDirectory)
        if(!dir.exists()) {
            dir.parentFile.mkdirs()
        }

        if(extractedFiles != null) {
            project.copy { CopySpec s ->
                s.from(project.files { extractedFiles.files })
                s.into(dir)
            }
        }
    }
}