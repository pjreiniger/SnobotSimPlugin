package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.os.OperatingSystem
import org.gradle.language.base.plugins.ComponentModelBasePlugin
import org.gradle.model.Mutate
import org.gradle.model.RuleSource

import groovy.transform.CompileStatic
import jaci.gradle.nativedeps.CombinedNativeLib
import jaci.gradle.nativedeps.NativeDepsSpec
import jaci.gradle.nativedeps.NativeLib


@CompileStatic
public class SnobotSimCppSetupRule implements Plugin<Project> {

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
                    lib.staticMatchers = ["**/*${libname}.lib".toString()]
                    lib.sharedMatchers = ["**/*${libname}.so".toString(), "**/*${libname}.dylib".toString()]

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

            createWpiLibrary('snobot_sim', "com.snobot.simulator:snobot_sim:2018-2.1.0", 'snobot_sim', true)
            createWpiLibrary('snobot_sim_jni', "com.snobot.simulator:snobot_sim_jni:2018-2.1.0", 'snobot_sim_jni', true)
            createWpiLibrary('snobot_sim_cpp_standalone', "com.snobot.simulator:snobot_sim_cpp_standalone:2018-2.1.0", 'snobot_sim_cpp_standalone', true)

            createWpiLibrary('adx_family', "com.snobot.simulator:adx_family:2018-2.1.0", 'adx_family', true)
            createWpiLibrary('navx_simulator', "com.snobot.simulator:navx_simulator:2018-2.1.0", 'navx_simulator', true)
            createWpiLibrary('halsim-adx_gyro_accelerometer', "edu.wpi.first.halsim:halsim-adx_gyro_accelerometer:2018.4.1", 'halsim_adx_gyro_accelerometer', true)

            libs.create("snobot_sim_jni_cpp_wrapper_headers", NativeLib) { NativeLib lib ->
                common(lib)
                lib.targetPlatforms << 'desktop'
                lib.headerDirs << ''
                lib.maven = "com.snobot.simulator:snobot_sim_jni:2018-2.1.0:cpp_jni_wrapper-headers@zip"
            }



            libs.create('snobot_sim_cpp', CombinedNativeLib) { CombinedNativeLib clib ->
                clib.libs <<
                        "snobot_sim" <<
                        "snobot_sim_cpp_standalone" <<
                        "snobot_sim_jni_cpp_wrapper_headers" <<
                        "adx_family" <<
                        "navx_simulator"

                clib.targetPlatforms = ['desktop']
                null
            }
        }
    }
}
