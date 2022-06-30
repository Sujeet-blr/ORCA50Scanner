package in.mobiux.android.orca50scanner.assetmanagementlite.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Asset extends BaseModel {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("asset")
    @Expose
    private String epc;
    @SerializedName("department")
    @Expose
    private int department;
    @SerializedName("createdAt")
    @Expose
    private long createdAt = System.currentTimeMillis();
    @SerializedName("time")
    @Expose
    private long time = 0;
    @SerializedName("name")
    @Expose
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdateTimeIntervalInSeconds() {
        long seconds = (System.currentTimeMillis() - getCreatedAt()) / 1000;
        return seconds;
    }

    public long getTime() {
        time = (System.currentTimeMillis() - getCreatedAt()) / 1000;
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "" + getName();
    }
}
