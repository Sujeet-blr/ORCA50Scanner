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
    private String inventoryId = "ID"+String.valueOf(new Random().nextInt(999));

    @PrimaryKey()
    @NonNull
    @SerializedName("epc")
    @Expose
    private String epc;

    @SerializedName("quantity")
    @Expose
    private int quantity = new Random().nextInt(99);

    @SerializedName("rssi")
    @Expose
    private String rssi;

//    @SerializedName("createdAt")
//    @Expose
//    private Date createdAt;

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
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
}
