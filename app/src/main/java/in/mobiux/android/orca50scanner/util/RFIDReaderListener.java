package in.mobiux.android.orca50scanner.util;

import com.module.interaction.RXTXListener;

import in.mobiux.android.orca50scanner.api.model.Inventory;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener {

    void onInventoryTag(Inventory inventory);

    void onConnection(boolean status);
}
