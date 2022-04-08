package in.mobiux.android.orca50scanner.sgul.api.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Entity(tableName = "inventory")
public class Inventory extends BaseModel implements Serializable {

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
    @SerializedName("rfid_label_name")
    @Expose
    private String rfidLabelName = "";
    @SerializedName("model")
    @Expose
    private String model = "";
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
    @SerializedName("checked")
    @Expose
    private boolean checked = false;

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

    public String getRfidLabelName() {
        return rfidLabelName;
    }

    public void setRfidLabelName(String rfidLabelName) {
        this.rfidLabelName = rfidLabelName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
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

