package in.mobiux.android.orca50scanner.stocklitev2.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import in.mobiux.android.commonlibs.utils.AppUtils;
import in.mobiux.android.orca50scanner.stocklitev2.utils.Util;

@Entity(tableName = "inventory")
public class Inventory extends in.mobiux.android.orcaairlibs.model.Inventory {


    @SerializedName("inventoryId")
    @Expose
    private int inventoryId;

    @PrimaryKey()
    @NonNull
    @SerializedName("epc")
    @Expose
    private String epc;

    @SerializedName("quantity")
    @Expose
    private int quantity = 0;
    @SerializedName("isMatchingWithSample")
    @Expose
    private boolean isMatchingWithSample = false;
    @SerializedName("createdAt")
    @Expose
    private String createdAt = AppUtils.getFormattedTimestampUpToSeconds();

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isMatchingWithSample() {
        return isMatchingWithSample;
    }

    public void setMatchingWithSample(boolean matchingWithSample) {
        isMatchingWithSample = matchingWithSample;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    @NonNull
    public String getEpc() {
        return epc;
    }

    @Override
    public void setEpc(@NonNull String epc) {
        this.epc = epc;
    }
}
