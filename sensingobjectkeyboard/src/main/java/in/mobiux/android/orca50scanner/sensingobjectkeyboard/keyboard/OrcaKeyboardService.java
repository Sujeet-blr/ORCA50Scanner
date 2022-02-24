package in.mobiux.android.orca50scanner.sensingobjectkeyboard.keyboard;

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

import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.reader.utils.BeeperHelper;
import in.mobiux.android.orca50scanner.sensingobjectkeyboard.R;
import in.mobiux.android.orca50scanner.sensingobjectkeyboard.util.MyApplication;


public class OrcaKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, Consumer<String> {

    private static final String TAG = OrcaKeyboardService.class.getCanonicalName();

    private KeyboardView kv;
    private Keyboard keyboard;
    private InputConnection ic;
    Toast toast;

    private boolean isCaps = false;
    private MyApplication app;
    private static AppLogger logger;
    private String rfid = "";
    public static boolean isKeyboardVisible = false;
    private int KEYCODE_PRESSED = 0;
    public static final int KEYCODE_RFID = 201;
    public static final int KEYCODE_BARCODE = 202;
    public static final int KEYCODE_LOGO = 204;
    private int count = 0;

    private String filterKey = "000000";


    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener = null;

    private com.zebra.sdl.Reader barcodeReader;
    private Reader.ReaderType readerType = Reader.ReaderType.RFID;
    private Timer timer = new Timer();

    @Override
    public View onCreateInputView() {

        app = (MyApplication) getApplicationContext();
        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "Keyboard is created");
        isKeyboardVisible = true;
        SessionManager session = SessionManager.getInstance(app);

        BeeperHelper.init(this);

        createToast();

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);

        keyboard = new Keyboard(this, R.xml.qwerty_complete);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        initRFID();
        initBarcode();

        ic = this.getCurrentInputConnection();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isKeyboardVisible = isInputViewShown();
                if (isKeyboardVisible) {
                    count = 0;
                    if (readerType == Reader.ReaderType.RFID && rfidReader == null) {
                        initRFID();
                    } else if (readerType == Reader.ReaderType.BARCODE && barcodeReader == null) {
                        initBarcode();
                    }
                } else {
                    count++;
                    if ((count % 5) == 0) {
                        releaseAllReader();
                        count = 0;
                    }
                }
            }
        }, 0, 1000);

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
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_CANCEL:
                ic.deleteSurroundingText(255, 255);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                break;
            case KEYCODE_RFID:
//                when rfid button is clicked
                readerType = Reader.ReaderType.RFID;

                break;
            case KEYCODE_BARCODE:
//                when barcode is clicked
                readerType = Reader.ReaderType.BARCODE;

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (logger == null) {
            logger = AppLogger.getInstance(getApplicationContext());
        }
        logger.i(TAG, "keyDown " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_F4 && isKeyboardVisible) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        isKeyboardVisible = false;
        logger.i(TAG, "Keyboard Service is stopped");

        releaseAllReader();

        timer.cancel();
        showToast("keyboard closed");
        app.onTerminate();
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

    private void registerRFIDReaderListener() {
        logger.i(TAG, "registering Rfid listener");

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {
            }

            @Override
            public void onInventoryTag(Inventory inventory) {
                rfid = inventory.getFormattedEPC();
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {

                beep();
                logger.i(TAG, "on Inventory End " + tagEnd.mTotalRead);

                if (tagEnd.mTotalRead == 0)
                    rfid = "";

                if (rfid.startsWith(filterKey))
                    publishData(rfid);
            }

            @Override
            public void onConnection(boolean status) {
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

    private void createToast() {
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    }

    private void showToast(CharSequence message) {
        if (toast == null) {
            createToast();
        }
        toast.setText(message);
        toast.show();
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

    private void initRFID() {
        readerType = Reader.ReaderType.RFID;
        logger.i(TAG, "Key Pressed RFID");

        showToast("RFID Reader");

        rfidReader = new RFIDReader(getApplicationContext());
        if (rfidReader.isConnected()) {
            logger.i(TAG, "Rfid is already connected");
            rfidReader.enableRfidReader(true);
        } else {
            logger.i(TAG, "RFID Connection initializing");
            rfidReader.connect(Reader.ReaderType.RFID);
            registerRFIDReaderListener();
        }
    }

    private void initBarcode() {
        showToast("Barcode Reader");
        readerType = Reader.ReaderType.BARCODE;

        if (barcodeReader == null)
            barcodeReader = new Barcode4710();
        barcodeReader.open(OrcaKeyboardService.this);
        barcodeReader.setResultCallback(this);
    }

    private void releaseAllReader() {
        if (rfidReader != null) {
            logger.i(TAG, "releasing rfid reader");
            rfidReader.releaseResources();
            rfidReader.unregisterListener(rfidReaderListener);
        }

        rfidReader = null;

        if (barcodeReader != null)
            barcodeReader.close();
        barcodeReader = null;
    }
}