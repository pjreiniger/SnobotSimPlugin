package com.snobot.simulator.plugin;

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import edu.wpi.first.gradlerio.wpi.WPIExtension
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
    private final List<String> mCppLibraries;
    private final List<String> mCppJavaLibraries;

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
        mCppLibraries = new ArrayList<>();
        mCppJavaLibraries = new ArrayList<>();

        mMavenRepos = new HashMap<>();
    }

    public void loadSnobotSimConfig(Project project, WPIDepsExtension wpiDeps) {
        File directory = wpiDeps.wpi.project.file("snobotsim/SnobotSim.json")

        if(directory.exists()) {
            directory.withReader {
                try {
                    SnobotSimDependencyConfigs snobotSimConfig = parseJson(mSlurper.parse(it));
                    convertLibrariesToString(snobotSimConfig, wpiDeps.wpi, wpiDeps.vendor.getDependencies())
                    if(snobotSimConfig.maven_repos == null) {
                        LOGGER.error("No SnobotSim maven repositories in the config file, this ain't gonna work!")
                    }
                    else {
                        mMavenRepos.putAll(snobotSimConfig.maven_repos);
                        LOGGER.info("SnobotSim adding maven repos " + mMavenRepos)
                    }
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

    private static String sanitizeDependency(String input, String versionNumber, String snobotSimVersion, WPIExtension wpiExtension, String nativeClassifier) {
        String output = input.replaceAll('\\$\\{version_number\\}', versionNumber)
        output = output.replaceAll('\\$\\{nativeclassifier\\}', nativeClassifier)
        output = output.replaceAll('\\$\\{wpilibVersion\\}', wpiExtension.wpilibVersion)
        output = output.replaceAll('\\$\\{wpiOpenCvVersion\\}', wpiExtension.opencvVersion)

        return output;
    }

    private void convertLibrariesToString(LibraryTuple configs, String versionNumber, String snobotSimVersion, WPIExtension wpiExtension, String nativeClassifier) {
        configs.java.each { String dep ->
            mJavaLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpiExtension, nativeClassifier))
        }
        configs.jni.each { String dep ->
            mJniLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpiExtension, nativeClassifier))
        }
        if(configs.cpp != null) {
            configs.cpp.each { String dep ->
                mCppLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpiExtension, nativeClassifier))
            }
        }
        if(configs.cpp_java != null) {
            configs.cpp_java.each { String dep ->
                mCppJavaLibraries.add(sanitizeDependency(dep, versionNumber, snobotSimVersion, wpiExtension, nativeClassifier))
            }
        }
    }

    private void convertLibrariesToString(SnobotSimDependencyConfigs config, WPIExtension wpiExtension, List<JsonDependency> wpiVendors) {

        convertLibrariesToString(config.third_party.libraries, "", "", wpiExtension, "")
        convertLibrariesToString(config.snobot_sim.libraries, config.snobot_sim.version_number, config.snobot_sim.version_number, wpiExtension, nativeclassifier)
        //        convertLibrariesToString(config.third_party.libraries)

        Map<String, SingleVendorVersionExtensionConfig> bestVendorProps = config.getBestVendorVersions(config.required_third_party, wpiVendors)
        bestVendorProps.values().each { SingleVendorVersionExtensionConfig vendorConfig ->
            convertLibrariesToString(vendorConfig.libraries, vendorConfig.version_number, config.snobot_sim.version_number, wpiExtension, nativeclassifier)
        }
    }


    private static class LibraryTuple {
        String[] java;
        String[] jni;
        String[] cpp;
        String[] cpp_java;
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
        Map<String, String> maven_repos;
        List<String> required_third_party;

        public Map<String, SingleVendorVersionExtensionConfig> getBestVendorVersions(List<String> requiredThirdParty, List<JsonDependency> wpiVendors) {
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
                    output.put(dep.name, theVersion)
                }
                else {
                    LOGGER.warn("Couldn't find SnobotSim vendor dep definition for " + dep.name)
                }
            }

            for(String requiredLib : requiredThirdParty)
            {
                if(!output.containsKey(requiredLib))
                {
                    SingleVendorExtensionConfig ourCollection = vendor_props.vendors.get(requiredLib)
                    SingleVendorVersionExtensionConfig theVersion = ourCollection.versions.get(ourCollection.default_version)
                    LOGGER.info("SnobotSim requires the library '" + requiredLib + "' but you aren't using it, so it is using the default version of " + theVersion.version_number)
                    output.put(requiredLib, theVersion)
                }
            }

            return output;
        }
    }
}