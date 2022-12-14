package in.mobiux.android.orca50scanner.sologistics.model;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.reader.model.Inventory;

public class Stock {

    private String barcode;
    private List<Inventory> rfidTags = new ArrayList<>();

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
}
