package in.mobiux.android.orca50scanner.otsmobile.utils;

import android.content.Context;

public class TokenManger {

    private Context context;
    private SessionManager sessionManager;

    public static final String ApplicationID = "rfidscanner";
    private static TokenManger INSTANCE;

    public static TokenManger getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TokenManger(context);
        }
        return INSTANCE;
    }

    private TokenManger(Context context) {
        this.context = context;
        sessionManager = SessionManager.getInstance(context);
    }

    public String getUUID() {
        return sessionManager.deviceId();
    }

    public String getUniversalToken() {
        return sessionManager.getStringValue(SessionManager.KEY_UNIVERSAL_TOKEN);
    }

    public String getApplicationToken() {
        return sessionManager.getStringValue(SessionManager.KEY_APPLICATION_TOKEN);
    }

    public void setUniversalToken(String universalToken){
        sessionManager.setStringValue(SessionManager.KEY_UNIVERSAL_TOKEN, universalToken);
    }

    public void setApplicationToken(String applicationToken){
        sessionManager.setStringValue(SessionManager.KEY_APPLICATION_TOKEN, applicationToken);
    }

    public String getPartialAuthorizationToken() {
        String token = "ApplicationId=" + ApplicationID + ",UniversalToken=" + getUniversalToken();
        token = "Universal " + Utils.encodeBase64(token);
        return token;
    }

    public String getFullAuthorizationToken() {
        String token = "ApplicationId=" + ApplicationID + ",ApplicationToken=" + getApplicationToken() + ",UniversalToken=" + getUniversalToken();
        return "Universal " + Utils.encodeBase64(token);
    }
}
