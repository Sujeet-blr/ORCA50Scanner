package in.mobiux.android.orca50scanner.sgul.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attribute extends BaseModel{

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("attr_type")
    @Expose
    private int attrType;
    @SerializedName("type_constraint")
    @Expose
    private int typeConstraint;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAttrType() {
        return attrType;
    }

    public void setAttrType(int attrType) {
        this.attrType = attrType;
    }

    public int getTypeConstraint() {
        return typeConstraint;
    }

    public void setTypeConstraint(int typeConstraint) {
        this.typeConstraint = typeConstraint;
    }
}
