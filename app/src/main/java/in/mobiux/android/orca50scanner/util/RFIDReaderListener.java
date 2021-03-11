package in.mobiux.android.orca50scanner.util;

import com.module.interaction.RXTXListener;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener extends RXTXListener {

    @Override
    void reciveData(byte[] bytes);

    @Override
    void sendData(byte[] bytes);

    @Override
    void onLostConnect();
}
