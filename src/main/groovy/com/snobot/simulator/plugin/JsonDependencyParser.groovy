package com.snobot.simulator.plugin;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import edu.wpi.first.gradlerio.wpi.dependencies.WPIDepsExtension
import edu.wpi.first.gradlerio.wpi.dependencies.WPIVendorDepsExtension.JsonDependency
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic


@CompileStatic
public class JsonDependencyParser {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonDependencyParser.class);

    private final JsonSlurper mSlurper;
    private final Map<String, String> mMavenRepos;
    private final List<String> mJavaLibraries;
    private final List<String> mJniLibraries;

    private String nativeclassifier = (
    OperatingSystem.current().isWindows() ?
    System.getProperty("os.arch") == 'amd64' ? 'windowsx86-64' : 'windowsx86' :
    OperatingSystem.current().isMacOsX() ? "osxx86-64" :
    OperatingSystem.current().isLinux() ? "linuxx86-64" :
    null
    )

    public JsonDependencyParser() {
        mSlurper = new JsonSlurper();
        mJavaLibraries = new ArrayList<>();
        mJniLibraries = new ArrayList<>();

        mMavenRepos = new HashMap<>();
        mMavenRepos.put("SnobotSim", "http://raw.githubusercontent.com/pjreiniger/maven_repo/master/")
        mMavenRepos.put("Wpi", "http://first.wpi.edu/FRC/roborio/maven/release/")
    }

    public void loadSnobotSimConfig(Project project, WPIDepsExtension wpiDeps) {
        File directory = wpiDeps.wpi.project.file("snobotsim/SnobotSim.json")

        if(directory.exists()) {
            directory.withReader {
                try {
                    SnobotSimDependencyConfigs snobotSimConfig = parseJson(mSlurper.parse(it));
                    convertLibrariesToString(snobotSimConfig, wpiDeps.wpi.wpilibVersion, wpiDeps.vendor.getDependencies())
                } catch (ex) {
                    LOGGER.error("Could not parse json file!", ex)
                }
            }
        }
        else {
            LOGGER.warn("Could not find simulator config at '" + directory + "', make sure you put the latest from TODO");
        }
    }

    @CompileDynamic
    private SnobotSimDependencyConfigs parseJson(Object jsonObject) {
        return new SnobotSimDependencyConfigs(jsonObject);
    }

    private static String sanitizeDependency(String input, String versionNumber, String snobotSimVersion, String wpilibVersion, String nativeClassifier) {
        String output = input.replaceAll('\\$\\{version_number\\}', versionNumber)
        output = output.replaceAll('\\$\\{nativeclassifier\\}', nativeClassifier)
        output = output.replaceAll('\\$\\{wpilibVersion\\}', wpilibVersion)

        return output;
    }

    private void convertLibrariesToString(LibraryTuple configs, String versionNumber, String snobotSimVersion, String wpilibVersion, String nativeClassifier) {
        configs.java.each { String dep ->
            mJavaLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpilibVersion, nativeClassifier))
        }
        configs.jni.each { String dep ->
            mJniLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpilibVersion, nativeClassifier))
        }
    }

    private void convertLibrariesToString(SnobotSimDependencyConfigs config, String wpilibVersion, List<JsonDependency> wpiVendors) {

        convertLibrariesToString(config.third_party.libraries, "", "", "", "")
        convertLibrariesToString(config.snobot_sim.libraries, config.snobot_sim.version_number, config.snobot_sim.version_number, wpilibVersion, nativeclassifier)
        //        convertLibrariesToString(config.third_party.libraries)

        Map<String, SingleVendorVersionExtensionConfig> bestVendorProps = config.getBestVendorVersions(wpiVendors)
        bestVendorProps.values().each { SingleVendorVersionExtensionConfig vendorConfig ->
            convertLibrariesToString(vendorConfig.libraries, vendorConfig.version_number, config.snobot_sim.version_number, wpilibVersion, nativeclassifier)
        }
    }


    private static class LibraryTuple {
        String[] java;
        String[] jni;
    }

    private static class SnobotSimCoreDependencyConfigs {
        String version_number;
        LibraryTuple libraries;
    }

    private static class SnobotSim3rdPartyDependencyConfigs {
        LibraryTuple libraries;
    }

    private static class SingleVendorVersionExtensionConfig {
        String version_number;
        LibraryTuple libraries;
    }

    private static class SingleVendorExtensionConfig {
        String default_version
        Map<String, SingleVendorVersionExtensionConfig> versions
    }

    private static class SnobotSimVendorProps {
        Map<String, SingleVendorExtensionConfig> vendors;
    }

    private static class SnobotSimDependencyConfigs {
        SnobotSimCoreDependencyConfigs snobot_sim;
        SnobotSim3rdPartyDependencyConfigs third_party;
        SnobotSimVendorProps vendor_props;

        public Map<String, SingleVendorVersionExtensionConfig> getBestVendorVersions(List<JsonDependency> wpiVendors) {
            Map<String, SingleVendorVersionExtensionConfig> output = new HashMap();

            wpiVendors.each { JsonDependency dep ->
                if(vendor_props.vendors.containsKey(dep.name)) {
                    SingleVendorExtensionConfig ourCollection = vendor_props.vendors.get(dep.name)

                    String bestVersionKey;
                    if(ourCollection.versions.containsKey(dep.version)) {
                        bestVersionKey = dep.version
                    }
                    else {
                        LOGGER.warn("The version specified in the vendor props (" + dep.version + ") was not found in the SnobotSim definition. " +
                                "Using default value of '" + ourCollection.default_version + "'")

                        bestVersionKey = ourCollection.default_version
                    }
                    SingleVendorVersionExtensionConfig theVersion = ourCollection.versions.get(bestVersionKey)
                    theVersion.version_number = bestVersionKey + "_" + theVersion.version_number
                    output.put(dep.name, theVersion)
                }
                else {
                    LOGGER.warn("Couldn't find SnobotSim vendor dep definition for " + dep.name)
                }
            }

            return output;
        }
    }
}