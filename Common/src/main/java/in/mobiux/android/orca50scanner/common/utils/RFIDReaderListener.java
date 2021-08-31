package in.mobiux.android.orca50scanner.common.utils;

import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;

import in.mobiux.android.orca50scanner.api.model.Inventory;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener {

    void onScanningStatus(boolean status);

    void onInventoryTag(Inventory inventory);

    void onOperationTag(RXOperationTag operationTag);

    void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd);

    void onConnection(boolean status);
}
