package in.mobiux.android.orca50scanner.sgul.api;

public class Endpoints {

    //    SOInventory_SGUL_Staging_v01.apk - IP pointing to staging server
    public static final String BASE_URL_STAGING = "https://demo.hito.solutions/api/v1/";

    //    SOInventory_SGUL_Release_extIP_v01.apk - IP pointing to SGUL server (external IP)
    public static final String BASE_URL_GLOBAL = "https://stgeorge.hitoitsolutions.com/api/v1/";

    //    SOInventory_SGUL_Release_intIP_v01.apk - IP pointing to SGUL server (internal IP)
    public static final String BASE_URL_LOCAL = "https://172.21.78.80/api/v1/";


//    public static final String BASE_URL = BASE_URL_STAGING;

    public static final String LOGIN = "auth/login/";
    public static final String DEPARTMENTS = "units/all/?flat_list=false";
    public static final String ASSETS = "assets/?rfid=True";
    public static final String UPDATE_ASSETS = "assets/device-scan/";

    public static final String UPDATE_ASSETS_NEW = "assets/device-scan/";

    public static final String LOGS = "tracking/device-log/";

}
