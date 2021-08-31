package in.mobiux.android.orca50scanner.reader.core;


import in.mobiux.android.orca50scanner.reader.model.Barcode;

public interface BarcodeReaderListener {


    void onConnection(boolean status);

    void onScanSuccess(Barcode barcode);

    void onScanFailed(Object o);

    void onScanningStatus(boolean status);

}
