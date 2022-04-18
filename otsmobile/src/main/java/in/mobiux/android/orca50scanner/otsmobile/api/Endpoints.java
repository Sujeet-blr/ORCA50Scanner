package in.mobiux.android.orca50scanner.otsmobile.api;

public class Endpoints {

    //    IP pointing to SSO server
    public static final String BASE_URL_AUTH = "https://universal-api.ultimafurniture.co.uk";

    //    endpoint pointing to staging server
    public static final String BASE_URL_STAGING = "https://mcs-api.ultimafurniture.co.uk";
    //    endpoint for item inventory
    public static final String BASE_URL_PRODUCTION = "https://mcs-api.ultimafurniture.co.uk";


    public static final String BASE_URL = BASE_URL_STAGING;

    public static String getAuthBaseUrl() {
        return BASE_URL_AUTH;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }


    //    User authentication endpoints
    public static final String LOGIN = "authenticate";
    public static final String UNIVERSAL_TOKEN = "authenticate/create-token";
    public static final String VALIDATE_APPLICATION_TOKEN = "authenticate/validate/application";
    public static final String LOGOUT = "authenticate/logout";

    //    process endpoints
    public static final String PROCESS_POINTS = "process/points";
    public static final String SCAN = "scan/items";


    public static final String LOGS = "tracking/device-log/";

}
