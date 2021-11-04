package in.mobiux.android.orca50scanner.reader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;

/**
 * Created by SUJEET KUMAR on 21-May-21.
 */
public class Barcode extends BaseModel {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("hex")
    @Expose
    private String hex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHex() {
        hex = AppUtils.generateHexEPC(getName());
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    @Override
    public String toString() {
        return getName();
    }
}
