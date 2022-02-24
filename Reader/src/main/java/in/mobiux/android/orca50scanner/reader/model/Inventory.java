package in.mobiux.android.orca50scanner.reader.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.io.Serializable;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Entity(tableName = "inventory")
//public class Inventory extends RXInventoryTag implements Serializable {
public class Inventory implements Serializable {

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
    @SerializedName("barcode")
    @Expose
    private String barcode;

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
    @SerializedName("isMatchingWithSample")
    @Expose
    private boolean isMatchingWithSample = false;

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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public boolean isMatchingWithSample() {
        return isMatchingWithSample;
    }

    public void setMatchingWithSample(boolean matchingWithSample) {
        isMatchingWithSample = matchingWithSample;
    }

    public String getFormattedEPC() {
//        return getEpc().replace(" ", "");
        return AppUtils.getFormattedEPC(getEpc());
    }


    public static Inventory getMatchingInventory(String strEPC, List<Inventory> list) {
        String formattedEPC = strEPC.replace(" ", "");
        for (Inventory inventory : list) {
            if (formattedEPC.equals(inventory.getFormattedEPC())) {
                return inventory;
            }
        }
        return null;
    }

//    @Override
//    public String toString() {
//        return "" + name;
//    }

    public static class InventoryTagEnd {
        public int mCurrentAnt = 0;
        public int mTagCount = 0;
        public int mReadRate = 0;
        public int mTotalRead = 0;
        public byte cmd = 0;

        public InventoryTagEnd() {
        }

        public InventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            mCurrentAnt = tagEnd.mCurrentAnt;
            mTagCount = tagEnd.mTagCount;
            mReadRate = tagEnd.mReadRate;
            mTotalRead = tagEnd.mTotalRead;
            cmd = tagEnd.cmd;
        }
    }
}

