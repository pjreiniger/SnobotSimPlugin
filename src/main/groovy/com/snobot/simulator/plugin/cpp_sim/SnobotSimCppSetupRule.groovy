package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.os.OperatingSystem
import org.gradle.language.base.plugins.ComponentModelBasePlugin
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.transform.CompileStatic
import jaci.gradle.nativedeps.CombinedNativeLib
import jaci.gradle.nativedeps.NativeDepsSpec
import jaci.gradle.nativedeps.NativeLib


@CompileStatic
public class SnobotSimCppSetupRule implements Plugin<Project> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void apply(Project project) {
        project.pluginManager.apply(ComponentModelBasePlugin)
    }

    static class WPIDepRules extends RuleSource {

        @Mutate
        void setupSnobotSimDeps(NativeDepsSpec libs, final ExtensionContainer extensionContainer) {

            def common = { NativeLib lib ->
                lib.targetPlatforms = ['roborio']
                lib.headerDirs = []
                lib.sourceDirs = []
                lib.staticMatchers = []
            }

            def nativeclassifier = (
                    OperatingSystem.current().isWindows() ?
                    System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
                    OperatingSystem.current().isMacOsX() ? "osxx86-64" :
                    OperatingSystem.current().isLinux() ? "linuxx86-64" :
                    null
                    )

            def createWpiLibrary = { String name, String mavenBase, String libname, boolean supportNative ->
                libs.create("${name}_headers", NativeLib) { NativeLib lib ->
                    common(lib)
                    lib.targetPlatforms << 'desktop'
                    lib.headerDirs << ''
                    lib.maven = "${mavenBase}:headers@zip"
                }

                libs.create("${name}_native", NativeLib) { NativeLib lib ->
                    common(lib)
                    lib.libraryName = "${name}_binaries"
                    lib.targetPlatforms = ['desktop']
                    lib.staticMatchers = ["**/shared/**/*${libname}.lib".toString()]
                    lib.sharedMatchers = ["**/shared/**/*${libname}.so".toString(), "**/shared/**/*${libname}.dylib".toString()]

                    lib.dynamicMatchers = lib.sharedMatchers + "**/${libname}.dll".toString()
                    lib.maven = "${mavenBase}:${nativeclassifier}@zip"
                }

                libs.create("${name}_sources", NativeLib) { NativeLib lib ->
                    common(lib)
                    lib.targetPlatforms << 'desktop'
                    lib.sourceDirs << ''
                    lib.maven = "${mavenBase}:sources@zip"
                }


                libs.create(name, CombinedNativeLib) { CombinedNativeLib lib ->
                    lib.libs << "${name}_binaries".toString() << "${name}_headers".toString()
                    lib.targetPlatforms << 'desktop'
                    null
                }
            }

            //            def wpilibExt = extensionContainer.getByType(WPIExtension)
            //            def snobotSimExt = extensionContainer.getByType(SnobotSimulatorVersionsExtension)
            //
            //            createWpiLibrary('snobot_sim', "com.snobot.simulator:snobot_sim:${snobotSimExt.snobotSimVersion}", 'snobot_sim', true)
            //            createWpiLibrary('adx_family', "com.snobot.simulator:adx_family:${snobotSimExt.snobotSimVersion}", 'adx_family', true)
            //            createWpiLibrary('navx_simulator', "com.snobot.simulator:navx_simulator:${snobotSimExt.snobotSimVersion}", 'navx_simulator', true)
            //
            //            // Not done with GradleRIO
            //            createWpiLibrary('halsim-adx_gyro_accelerometer', "edu.wpi.first.halsim:halsim_adx_gyro_accelerometer:${wpilibExt.wpilibVersion}", 'halsim_adx_gyro_accelerometer', true)
            //
            //            libs.create('snobot_sim_cpp', CombinedNativeLib) { CombinedNativeLib clib ->
            //                clib.libs <<
            //                        "snobot_sim" <<
            //                        "adx_family" <<
            //                        "navx_simulator" <<
            //                        "halsim-adx_gyro_accelerometer"
            //
            //                clib.targetPlatforms = ['desktop']
            //                null
            //            }
        }
    }
}
