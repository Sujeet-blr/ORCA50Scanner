package in.mobiux.android.orca50scanner.api.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Date;

import in.mobiux.android.orca50scanner.util.Converters;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Entity(tableName = "inventory")
public class Inventory extends BaseModel{

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("primaryKey")
    @Expose
    private int id;

    @SerializedName("inventoryId")
    @Expose
    private String inventoryId;

    @SerializedName("epc")
    @Expose
    private String epc;

    @SerializedName("quantity")
    @Expose
    private int quantity;

//    @SerializedName("createdAt")
//    @Expose
//    private Date createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

}
