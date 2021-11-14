package in.mobiux.android.orca50scanner.sensingobjectkeyboard.service;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;


import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReader;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.sensingobjectkeyboard.MyApplication;
import in.mobiux.android.orca50scanner.sensingobjectkeyboard.R;


public class OrcaKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private InputConnection ic;
    Toast toast;

    private boolean isCaps = false;
    private static final String TAG = OrcaKeyboardService.class.getCanonicalName();
    private MyApplication app;
    private AppLogger logger;
    private String rfid = "";
    private String barcode = "";
    public static boolean isKeyboardActive = false;
    private int KEYCODE_PRESSED = 0;
    private int SELECTED_READER = 0;
    public static final int KEYCODE_RFID = 201;
    public static final int KEYCODE_BARCODE = 202;
    public static final int KEYCODE_LOGO = 204;
    //    private ODScannerUtils odScannerUtils;

    private BarcodeReader barcodeReader;
    private BarcodeReaderListener barcodeReaderListener;

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;

    private Reader.ReaderType readerType;


    @Override
    public View onCreateInputView() {
//        return super.onCreateInputView();

        app = (MyApplication) getApplicationContext();
        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "Keyboard is created");
        isKeyboardActive = true;

        createToast();

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        barcodeReader = new BarcodeReader(getApplicationContext());
        barcodeReader.connect(Reader.ReaderType.BARCODE);
        registerBarcodeReaderListener();

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        registerRFIDReaderListener();

        ic = this.getCurrentInputConnection();

        return kv;
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int i, int[] ints) {

        ic = getCurrentInputConnection();
        playClick(i);

        KEYCODE_PRESSED = i;

        switch (i) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(255, 255);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KEYCODE_RFID:
//                when rfid is clicked
                SELECTED_READER = KEYCODE_RFID;
                readerType = Reader.ReaderType.RFID;

                if (rfidReader.isConnected()) {

                    if (readerType == Reader.ReaderType.RFID) {
                        showToast(app.getResources().getString(R.string.msg_rfid_already_connected));
                    }

                } else {
                    barcodeReader.releaseResources();
                    showToast(app.getResources().getString(R.string.msg_rfid_connect_initiated));
                    rfidReader.connect(Reader.ReaderType.RFID);
                }
                break;
            case KEYCODE_BARCODE:
                SELECTED_READER = KEYCODE_BARCODE;
                readerType = Reader.ReaderType.BARCODE;
//                showToast(R.string.not_implemented);

                if (barcodeReader.isConnected()) {
                    showToast(R.string.msg_barcode_already_connected);
                } else {
                    showToast(R.string.msg_barcode_initiated);
                    rfidReader.releaseResources();
                    barcodeReader.connect(Reader.ReaderType.BARCODE);
                }

                break;
            case 203:
                break;

            case KEYCODE_LOGO:
                Log.i(TAG, "Logo clicked");

//                if (ContextCompat.checkSelfPermission(app.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    Intent requestIntent = new Intent(app.getApplicationContext(), RequestPermissionActivity.class);
//                    requestIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(requestIntent);
//                    return;
//                }

//                logger.createAndExportLogs(app.getApplicationContext());

                break;

            case 209:
                kv.invalidateAllKeys();
                break;

            default:
                char code = (char) i;
                Log.i(TAG, String.valueOf(code));
                Log.i(TAG, String.valueOf(i));
                if (Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);
                ic.commitText(String.valueOf(code), 1);
        }
    }

    private void playClick(int i) {

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    private void registerBarcodeReaderListener() {

        barcodeReaderListener = new BarcodeReaderListener() {
            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "barcode status " + status);
                if (status) {
                    showToast(R.string.msg_barcode_connected);
                }
            }

            @Override
            public void onScanSuccess(Barcode b) {
                logger.i(TAG, "barcode " + b.getName());
                barcode = b.getName();


                if (isKeyboardActive == false)
                    return;

                if (ic == null) {
                    ic = getCurrentInputConnection();
                }

                if (ic != null && readerType == Reader.ReaderType.BARCODE) {
                    ic.deleteSurroundingText(255, 255);
                    ic.commitText(barcode, 1);

                    KeyEvent eventEnter = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
                    ic.sendKeyEvent(eventEnter);
                }

                showToast(R.string.msg_scanned);
            }

            @Override
            public void onScanFailed(Object o) {
                logger.i(TAG, "barcode failed");
                showToast("Scanned Failed");
            }

            @Override
            public void onScanningStatus(boolean status) {
                if (status) {
                    showToast(R.string.msg_barcode_scanning);
                }

            }
        };

        barcodeReader.setOnBarcodeReaderListener(barcodeReaderListener);
    }

    private void registerRFIDReaderListener() {

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {
                logger.i(TAG, "Scanning Status " + status);

                if (ic != null) {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, 209);
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, 209));
                }

                if (status && KEYCODE_PRESSED == KEYCODE_RFID) {
                    showToast(app.getResources().getString(R.string.msg_rfid_scanning));
                } else if (status && KEYCODE_PRESSED == KEYCODE_BARCODE) {
                    showToast(app.getResources().getString(R.string.msg_barcode_scanning));
                }
            }

            @Override
            public void onInventoryTag(Inventory inventory) {
                rfid = inventory.getFormattedEPC();
                logger.i(TAG, "inventory found " + inventory.getFormattedEPC());
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {
                logger.i(TAG, "onOperation Tag " + operationTag.strEPC);
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
                app.playBeep();
                logger.i(TAG, "on Inventory End");

                if (isKeyboardActive == false)
                    return;

                if (ic == null) {
                    ic = getCurrentInputConnection();
                }

                if (ic != null && readerType == Reader.ReaderType.RFID) {
                    ic.deleteSurroundingText(255, 0);
                    ic.commitText(rfid, 1);

                    KeyEvent eventEnter = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
                    ic.sendKeyEvent(eventEnter);
                }

//                showToast(R.string.msg_scanned);

            }

            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "Connection Status " + status);
                if (status) {
                    if (readerType == Reader.ReaderType.RFID) {
                        showToast(app.getResources().getString(R.string.msg_rfid_connected));
                    } else if (readerType == Reader.ReaderType.BARCODE) {
                        showToast(app.getResources().getString(R.string.msg_barcode_connected));
                    }

                } else {
                    showToast(app.getResources().getString(R.string.msg_rfid_selection_required));
                }
            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isKeyboardActive = false;
//        app.releaseResources();
        logger.i(TAG, "Keyboard Service is stopped");


        if (barcodeReader != null) {
            barcodeReader.releaseResources();
        }

        if (rfidReader != null) {
            rfidReader.releaseResources();
        }

        showToast("keyboard closed");
        app.onTerminate();
    }

    private void createToast() {
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    }

    private void showToast(CharSequence message) {
        toast.setText(message);
        toast.show();
    }

    private void showToast(int resId) {
        toast.setText(resId);
        toast.show();
    }
}