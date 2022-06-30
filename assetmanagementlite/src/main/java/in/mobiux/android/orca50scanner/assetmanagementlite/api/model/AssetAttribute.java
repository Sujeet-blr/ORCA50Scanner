package in.mobiux.android.orca50scanner.assetmanagementlite.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AssetAttribute extends BaseModel{

    @SerializedName("attribute")
    @Expose
    private Attribute attributes;
    @SerializedName("value")
    @Expose
    private Value values;

    public Attribute getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute attributes) {
        this.attributes = attributes;
    }

    public Value getValues() {
        return values;
    }

    public void setValues(Value values) {
        this.values = values;
    }
}
