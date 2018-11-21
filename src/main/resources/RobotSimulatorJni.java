

public class RobotSimulatorJni
{

    public static String getLibraryName()
    { 
        return "snobotSimCppWrapper";
    }

    public static native void createRobot();
    public static native void startCompetition();
}