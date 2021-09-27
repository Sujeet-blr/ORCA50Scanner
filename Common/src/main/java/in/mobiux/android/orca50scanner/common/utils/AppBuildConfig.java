package in.mobiux.android.orca50scanner.common.utils;

public class AppBuildConfig {

    public static boolean DEBUG = Boolean.parseBoolean("true");
    public static String APPLICATION_ID = "in.mobiux.android.orca50scanner";
    public static String BUILD_TYPE = "debug";
    public static int VERSION_CODE = 28;
    public static String VERSION_NAME = "A01_RFID_Reader";

    public static boolean isDEBUG() {
//        return DEBUG;
        return false;
    }

}
