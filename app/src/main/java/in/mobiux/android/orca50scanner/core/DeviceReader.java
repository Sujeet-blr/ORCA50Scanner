package in.mobiux.android.orca50scanner.core;

public interface DeviceReader {
    enum ReaderType {
        RFID, BARCODE;
    }

    void connect(ReaderType type);
    boolean isConnected();
}
