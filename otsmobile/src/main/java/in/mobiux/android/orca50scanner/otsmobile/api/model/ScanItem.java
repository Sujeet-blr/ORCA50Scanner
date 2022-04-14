package in.mobiux.android.orca50scanner.otsmobile.api.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import in.mobiux.android.orca50scanner.otsmobile.utils.Utils;

@Entity(tableName = "scanItems")
public class ScanItem extends BaseModel {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("orderItemId")
    @Expose
    private String orderItemId;
    @SerializedName("processPointId")
    @Expose
    private int processPointId;
    @SerializedName("scanTime")
    @Expose
    private String scanTime = Utils.getFormattedCurrentTime();
    @SerializedName("scanType")
    @Expose
    private String scanType;
    @SerializedName("uploaded")
    @Expose
    private boolean uploaded;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessPointId() {
        return processPointId;
    }

    public void setProcessPointId(int processPointId) {
        this.processPointId = processPointId;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isValidItem() {
        boolean status = false;

        if (orderItemId.length() != 24) {
            return false;
        } else {
            if (orderItemId.startsWith("FD") || orderItemId.startsWith("fd")
                    || orderItemId.startsWith("fD") || orderItemId.startsWith("Fd")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
