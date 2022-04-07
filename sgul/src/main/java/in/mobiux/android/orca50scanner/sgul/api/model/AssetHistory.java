package in.mobiux.android.orca50scanner.sgul.api.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SUJEET KUMAR on 21-Apr-21.
 */
@Entity(tableName = "assetHistory")
public class AssetHistory extends BaseModel {

    @PrimaryKey(autoGenerate = true)
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
}
