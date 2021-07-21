package in.mobiux.android.orca50scanner.api.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.io.Serializable;
import java.sql.Date;
import java.util.Random;
import java.util.UUID;

import in.mobiux.android.orca50scanner.util.Converters;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Entity(tableName = "inventory")
public class Inventory extends RXInventoryTag implements Serializable {

//    @PrimaryKey(autoGenerate = true)
//    @NonNull
//    @SerializedName("primaryKey")
//    @Expose
//    private int id;

    @SerializedName("inventoryId")
    @Expose
    private int inventoryId;
//    private String inventoryId = "ID" + String.valueOf(new Random().nextInt(999));

    @PrimaryKey()
    @NonNull
    @SerializedName("epc")
    @Expose
    private String epc;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("labId")
    @Expose
    private int labId;

    @SerializedName("laboratoryName")
    @Expose
    private String laboratoryName;

    @SerializedName("quantity")
    @Expose
    private int quantity = 0;

    @SerializedName("rssi")
    @Expose
    private String rssi;

    @SerializedName("locationAssigned")
    @Expose
    private boolean locationAssigned = false;
    @SerializedName("syncRequired")
    @Expose
    private boolean syncRequired = false;
    @SerializedName("updatedAt")
    @Expose
    private long updatedAt = System.currentTimeMillis();
    @SerializedName("scanStatus")
    @Expose
    private boolean scanStatus = false;

//    @SerializedName("createdAt")
//    @Expose
//    private Date createdAt;


    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public int getLabId() {
        return labId;
    }

    public void setLabId(int labId) {
        this.labId = labId;
    }

    public String getLaboratoryName() {
        return laboratoryName;
    }

    public void setLaboratoryName(String laboratoryName) {
        this.laboratoryName = laboratoryName;
    }

    public boolean isLocationAssigned() {
        return locationAssigned;
    }

    public void setLocationAssigned(boolean locationAssigned) {
        this.locationAssigned = locationAssigned;
    }

    public boolean isSyncRequired() {
        return syncRequired;
    }

    public void setSyncRequired(boolean syncRequired) {
        this.syncRequired = syncRequired;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(boolean scanStatus) {
        this.scanStatus = scanStatus;
    }

    public long getUpdateTimeIntervalInSeconds() {
        long seconds = (System.currentTimeMillis() - getUpdatedAt()) / 1000;
        return seconds;
    }

    public String getFormattedEPC() {
        return getEpc().replace(" ", "");
    }

    @Override
    public String toString() {
        return "" + name;
    }
}

