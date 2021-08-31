package in.mobiux.android.orca50scanner.reader.core;

public interface Reader {
    enum ReaderType {
        RFID, BARCODE;
    }

    void connect(ReaderType type);
    boolean isConnected();
}
