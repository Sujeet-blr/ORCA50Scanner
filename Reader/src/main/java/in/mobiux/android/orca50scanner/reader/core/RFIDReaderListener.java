package in.mobiux.android.orca50scanner.reader.core;

import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;

import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;


/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener {

    void onScanningStatus(boolean status);

    void onInventoryTag(Inventory inventory);

    void onOperationTag(OperationTag operationTag);

    void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd);

    void onConnection(boolean status);
}
