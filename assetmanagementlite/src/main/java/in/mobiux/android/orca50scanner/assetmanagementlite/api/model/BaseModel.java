package in.mobiux.android.orca50scanner.assetmanagementlite.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
class BaseModel implements Serializable {
    @SerializedName("message")
    @Expose
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
