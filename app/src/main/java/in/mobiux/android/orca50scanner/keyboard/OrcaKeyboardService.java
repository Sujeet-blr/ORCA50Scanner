package in.mobiux.android.orca50scanner.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;

public class OrcaKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, RFIDReaderListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private InputConnection ic;

    private boolean isCaps = false;
    private static final String TAG = OrcaKeyboardService.class.getCanonicalName();
    private MyApplication app;
    private AppLogger logger;
    private String rfid = "";
    private String barcode = "";

    @Override
    public View onCreateInputView() {
//        return super.onCreateInputView();

        app = (MyApplication) getApplicationContext();
        logger = AppLogger.getInstance(getApplicationContext());

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        app.setOnRFIDListener(this);

        ic = getCurrentInputConnection();

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

        switch (i) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 201:
//                when rfid is clicked
//                ic.commitText("this is rfid clicked", 1);
//                String rfid = Settings.Global.getString(getContentResolver(), "rfid");
                ic.commitText(rfid, 1);
                break;
            case 202:
                ic.commitText(barcode, 1);
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

    @Override
    public void onInventoryTag(Inventory inventory) {

        ic = getCurrentInputConnection();
        if (ic != null) {
            ic.deleteSurroundingText(255, 0);
            rfid = inventory.getEpc();
            ic.commitText(rfid, 1);
        }

        logger.i(TAG, "inventory found " + inventory.getEpc());

    }

    @Override
    public void onScanningStatus(boolean status) {
        logger.i(TAG, "Scanning Status " + status);
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        logger.i(TAG, "on Inventory End");
    }

    @Override
    public void onConnection(boolean status) {
        logger.i(TAG, "Connection Status " + status);
    }
}