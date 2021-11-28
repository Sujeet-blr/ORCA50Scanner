package in.mobiux.android.orca50scanner.sensingobjectrfidkeyboard.keyboard;

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

import com.zebra.model.Consumer;
import com.zebra.sdl.Barcode4710;

import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.reader.utils.BeeperHelper;
import in.mobiux.android.orca50scanner.sensingobjectrfidkeyboard.R;
import in.mobiux.android.orca50scanner.sensingobjectrfidkeyboard.util.MyApplication;


public class OrcaCompleteKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, Consumer<String> {

    private static final String TAG = OrcaCompleteKeyboardService.class.getCanonicalName();

    private KeyboardView kv;
    private Keyboard keyboard;
    private InputConnection ic;
    Toast toast;

    private boolean isCaps = false;
    private MyApplication app;
    private static AppLogger logger;
    private String rfid = "";
    public static boolean isKeyboardActive = false;
    private int KEYCODE_PRESSED = 0;
    private int SELECTED_READER = 0;
    public static final int KEYCODE_RFID = 201;
    public static final int KEYCODE_BARCODE = 202;
    public static final int KEYCODE_LOGO = 204;


    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener = null;

    private com.zebra.sdl.Reader barcodeReader;

    private Reader.ReaderType readerType = Reader.ReaderType.RFID;

    boolean isNumberLayout = false;


    @Override
    public View onCreateInputView() {

        app = (MyApplication) getApplicationContext();
        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "Keyboard is created");
        isKeyboardActive = true;

        BeeperHelper.init(this);

        createToast();

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        if (isNumberLayout) {
            keyboard = new Keyboard(this, R.xml.qwerty);
        } else {
            keyboard = new Keyboard(this, R.xml.qwerty_complete);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        rfidReader = new RFIDReader(getApplicationContext());

        if (rfidReader.isConnected()) {
            rfidReader.releaseResources();
        }

//        rfidReader.connect(Reader.ReaderType.RFID);
        ic = this.getCurrentInputConnection();

//        registerRFIDReaderListener();

        return kv;
    }

    @Override
    public void onPress(int i) {
        logger.i(TAG, "onPress " + i);
    }

    @Override
    public void onRelease(int i) {
        logger.i(TAG, "onRelease " + i);
    }

    @Override
    public void onKey(int i, int[] ints) {
        logger.i(TAG, "onKey " + i + " " + ints);

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
                logger.i(TAG, "Key Pressed RFID");

                if (barcodeReader != null) {
                    barcodeReader.close();
                }

                showToast("RFID Reader");

                if (rfidReader.isConnected()) {
                    logger.i(TAG, "Rfid is already connected");
                    rfidReader.enableRfidReader(true);
//                    showToast(app.getResources().getString(R.string.msg_rfid_already_connected));

                } else {
                    logger.i(TAG, "RFID Connection initializing");
//                    showToast(app.getResources().getString(R.string.msg_rfid_connect_initiated));
                    rfidReader.connect(Reader.ReaderType.RFID);
                    registerRFIDReaderListener();
                }


                break;
            case KEYCODE_BARCODE:

                showToast("Barcode Reader");

                readerType = Reader.ReaderType.BARCODE;
                SELECTED_READER = KEYCODE_BARCODE;

                rfidReader.enableRfidReader(false);

                if (barcodeReader == null)
                    barcodeReader = new Barcode4710();
                boolean barcodeConnectionStatus = barcodeReader.open(OrcaCompleteKeyboardService.this);
                barcodeReader.setResultCallback(this);

                logger.i(TAG, "Key Pressed BARCODE");

                break;
            case 203:
                break;

            case KEYCODE_LOGO:
                logger.i(TAG, "Key Pressed Export Logs");
//                Intent intent = new Intent(app, ExportLogsActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

                SettingsActivity.launchActivity(app);

                break;

            case 209:
                kv.invalidateAllKeys();
                break;
            default:
                logger.i(TAG, "key press " + KEYCODE_PRESSED);

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

        logger.i(TAG, "playClick " + i);
    }

    @Override
    public void onText(CharSequence charSequence) {
        logger.i(TAG, "onText " + charSequence);
    }

    @Override
    public void swipeLeft() {
        logger.i(TAG, "swipeLeft ");
    }

    @Override
    public void swipeRight() {
        logger.i(TAG, "swipeRight ");
    }

    @Override
    public void swipeDown() {
        logger.i(TAG, "swipeDown ");
    }

    @Override
    public void swipeUp() {
        logger.i(TAG, "swipeUp ");
    }

    private void registerRFIDReaderListener() {
        logger.i(TAG, "registering Rfid listener");

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {
//                logger.i(TAG, "Scanning Status " + status);
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

                beep();
                logger.i(TAG, "on Inventory End " + tagEnd.mTotalRead);

                if (tagEnd.mTotalRead == 0)
                    rfid = "";

                publishData(rfid);
            }

            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "Connection Status " + status);
                if (status) {
                    showToast(app.getResources().getString(R.string.msg_rfid_connected));
                } else {
                    showToast(app.getResources().getString(R.string.msg_rfid_selection_required));
                }
            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }

    private void beep() {
        BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isKeyboardActive = false;
        logger.i(TAG, "Keyboard Service is stopped");

        if (rfidReader != null) {
            logger.i(TAG, "releasing rfid reader");
            rfidReader.releaseResources();
            rfidReader.unregisterListener(rfidReaderListener);
        }

        if (barcodeReader != null)
            barcodeReader.close();

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        logger.i(TAG, "keyDown " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_F4) {

            if (readerType == Reader.ReaderType.RFID) {
                if (rfidReader != null && rfidReader.isConnected()) {
                    rfidReader.startScan();
                }
            } else if (readerType == Reader.ReaderType.BARCODE) {
                if (barcodeReader != null) {
                    barcodeReader.read(true);
                }
            } else {
                showToast("Please select Reader to scan");
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void accept(String s) throws Exception {
        logger.i(TAG, "Barcode is " + s);

        publishData(s);
        beep();
    }

    private void publishData(String str) {
        ic = getCurrentInputConnection();
        if (ic != null) {
            ic.deleteSurroundingText(255, 255);
            ic.commitText(str, 1);
            KeyEvent eventEnter = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
            ic.sendKeyEvent(eventEnter);
        }
    }
}