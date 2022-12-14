package in.mobiux.android.orca50scanner.stocklitev2.model;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.commonlibs.utils.AppUtils;
import in.mobiux.android.orca50scanner.stocklitev2.db.model.Inventory;

public class Stock {

    private String barcode;
    private List<Inventory> rfidTags = new ArrayList<>();
    private String timestamp = AppUtils.getFormattedTimestampUpToSeconds();

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<Inventory> getRfidTags() {
        return rfidTags;
    }

    public void setRfidTags(List<Inventory> rfidTags) {
        this.rfidTags = rfidTags;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
