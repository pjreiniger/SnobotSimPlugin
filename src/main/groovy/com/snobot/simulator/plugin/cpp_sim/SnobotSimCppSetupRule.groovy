package com.snobot.simulator.plugin.cpp_sim;

import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wpi.first.gradlerio.wpi.WPIExtension
import edu.wpi.first.toolchain.NativePlatforms
import groovy.transform.CompileStatic
import jaci.gradle.nativedeps.CombinedNativeLib
import jaci.gradle.nativedeps.NativeDepsSpec
import jaci.gradle.nativedeps.NativeLib

@CompileStatic
public class SnobotSimCppSetupRule extends RuleSource {
    protected static final Logger logger = LoggerFactory.getLogger(SnobotSimCppSetupRule.class);



    @Mutate
    void setupSnobotSimDeps(NativeDepsSpec libs, final ExtensionContainer extensionContainer) {
        def wpi = extensionContainer.getByType(WPIExtension)
        addSnobotSimLibraries(libs, wpi)
    }

    private static void common(NativeLib lib) {
        lib.targetPlatforms = []
        lib.headerDirs = []
        lib.sourceDirs = []
        lib.debugMatchers = ['**/*.pdb', '**/*.so.debug']
        lib.staticMatchers = []
    }

    private static void matchersShared(NativeLib lib, String libname, boolean desktop) {
        if (desktop) {
            lib.sharedMatchers = ["**/shared/*${libname}*.lib".toString(), "**/shared/*${libname}*.so".toString(), "**/shared/*${libname}*.dylib".toString()]
            lib.dynamicMatchers = lib.sharedMatchers + "**/shared/*${libname}*.dll".toString()
        } else {
            lib.sharedMatchers = ["**/*${libname}*.so".toString()]
            lib.dynamicMatchers = lib.sharedMatchers
        }
    }

    private static void matchersStatic(NativeLib lib, String libname, boolean desktop) {
        if (desktop) {
            lib.staticMatchers = ["**/static/*${libname}*.lib".toString(), "**/static/*${libname}*.a".toString()]
        } else {
            lib.staticMatchers = ["**/static/*${libname}*.a".toString()]
        }
    }

    private static void createSnobotSimLibrary(NativeDepsSpec libs, String name, String mavenBase, String libName, boolean shared) {

        ['debug', ''].each { String buildKind ->
            String buildType    = buildKind.contains('debug') ? 'debug' : 'release'
            String libSuffix    = buildKind.contains('debug') ? 'd' : ''
            String config       = "native_${name}${buildKind}".toString()
            String linkSuff     = shared ? '' : 'static'

            libs.create("${name}_headers${buildKind}".toString(), NativeLib, { NativeLib lib ->
                common(lib)
                lib.targetPlatforms << NativePlatforms.desktop
                lib.libraryName = "${name}_headers"
                lib.buildType = buildType
                lib.headerDirs.add('')
                lib.maven = "${mavenBase}:headers@zip"
                lib.configuration = config
            } as Action<? extends NativeLib>)

            libs.create("${name}_native${buildKind}".toString(), NativeLib, { NativeLib lib ->
                common(lib)
                if (shared)
                    matchersShared(lib, libName + libSuffix, true)
                else
                    matchersStatic(lib, libName + libSuffix, true)
                lib.targetPlatforms << NativePlatforms.desktop
                lib.libraryName = "${name}_binaries"
                lib.buildType = buildType
                lib.maven = "${mavenBase}:${NativePlatforms.desktop}${linkSuff}${buildKind}@zip"
                lib.configuration = "${config}_desktop"
            } as Action<? extends NativeLib>)

            libs.create("${name}_sources${buildKind}".toString(), NativeLib, { NativeLib lib ->
                common(lib)
                lib.targetPlatforms << NativePlatforms.desktop
                lib.libraryName = "${name}_sources"
                lib.buildType = buildType
                lib.sourceDirs << ''
                lib.maven = "${mavenBase}:sources@zip"
                lib.configuration = config
            } as Action<? extends NativeLib>)
        }

        libs.create(name, CombinedNativeLib, { CombinedNativeLib lib ->
            lib.libs << "${name}_binaries".toString() << "${name}_headers".toString() << "${name}_sources".toString()
            lib.buildTypes = ['debug', 'release']
            lib.targetPlatforms << NativePlatforms.desktop
        } as Action<? extends CombinedNativeLib>)
    }

    private static void addSnobotSimLibraries(NativeDepsSpec libs, final WPIExtension wpi) {
        for (boolean shared in [true, false]) {
            def suf = shared ? '' : '_static'

            String tmpVersion = "2019-0.0.1-RC"
            createSnobotSimLibrary(libs, 'xXxXxX' + suf, "com.snobot.simulator:snobot_sim:${tmpVersion}", 'xXxXxX', shared)

            libs.create('snobotSimCpp' + suf, CombinedNativeLib, { CombinedNativeLib lib ->
                lib.libs << 'xXxXxX' + suf
                lib.buildTypes = ['debug', 'release']
                lib.targetPlatforms = [wpi.platforms.desktop]
            } as Action<? extends CombinedNativeLib>)
        }
    }
}

