package in.mobiux.android.orca50scanner.util;

import com.module.interaction.RXTXListener;
import com.rfid.rxobserver.bean.RXInventoryTag;

import in.mobiux.android.orca50scanner.api.model.Inventory;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener {

    void onInventoryTag(Inventory inventory);

    void onScanningStatus(boolean status);

    void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd);

    void onConnection(boolean status);
}
