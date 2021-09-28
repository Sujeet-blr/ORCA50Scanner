package in.mobiux.android.orca50scanner.reader.core.barcode;


//this is for barcode reader scanner model SE4750

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReader;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Barcode;

public class BarcodeReaderOrca implements Reader, BarCodeReader.DecodeCallback, BarCodeReader.ErrorCallback {

    public static final String TAG = BarcodeReader.class.getCanonicalName();
    private final Handler handler;
    private Context context;
    private SessionManager session;
    private AppLogger logger;

    private BarcodeReaderListener listener;


    static final private boolean saveSnapshot = false; // true = save snapshot to file
    static private boolean sigcapImage = true; // true = display signature capture
    static private boolean videoCapDisplayStarted = false;

    //states
    static final int STATE_IDLE = 0;
    static final int STATE_DECODE = 1;
    static final int STATE_HANDSFREE = 2;
    static final int STATE_PREVIEW = 3;    //snapshot preview mode
    static final int STATE_SNAPSHOT = 4;
    static final int STATE_VIDEO = 5;

    // BarCodeReader specifics
    private BarCodeReader bcr = null;

    private boolean beepMode = true;        // decode beep enable
    private int Mobile_reading_pane = 716;        // Mobile Phone reading Pane
    private int reading_pane_value = 1;
    private boolean snapPreview = false;        // snapshot preview mode enabled - true - calls viewfinder which gets handled by
    private int trigMode = BarCodeReader.ParamVal.LEVEL;
    private boolean atMain = false;
    private int state = STATE_IDLE;
    private int decodes = 0;

    private int motionEvents = 0;
    private int modechgEvents = 0;

    private int snapNum = 0;        //saved snapshot #
    private String decodeDataString;
    private String decodeStatString;
    private static int decCount = 0;

    //add for test
    private long mStartTime;
    private long mBarcodeCount = 0;
    private long mConsumTime;

    private boolean mKeyF4Down = false;


    static {
        System.loadLibrary("IAL");
        System.loadLibrary("SDL");

        if (android.os.Build.VERSION.SDK_INT >= 19)
            System.loadLibrary("barcodereader44"); // Android 4.4
        else if (android.os.Build.VERSION.SDK_INT >= 18)
            System.loadLibrary("barcodereader43"); // Android 4.3
        else
            System.loadLibrary("barcodereader");   // Android 2.3 - Android 4.2
    }

    public BarcodeReaderOrca(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);
        session = SessionManager.getInstance(context);
        handler = new Handler(context.getMainLooper());

        // sound
//		tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

        BeeperHelper.init(context);
    }

    @Override
    public void connect(ReaderType type) {
        initScanner();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    private void initScanner() {
        state = STATE_IDLE;
        mKeyF4Down = false;
        try {

            int num = BarCodeReader.getNumberOfReaders();
//            dspStat(getResources().getString(R.string.app_name) + " v" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
            if (android.os.Build.VERSION.SDK_INT >= 18)
                bcr = BarCodeReader.open(num, context); // Android 4.3 and above
            else
                bcr = BarCodeReader.open(num); // Android 2.3

            if (bcr == null) {
//                dspErr("open failed");
                logger.e(TAG, "Open Failed");
                return;
            }

            bcr.setDecodeCallback(this);

            bcr.setErrorCallback(this);

            // Set parameter - Uncomment for QC/MTK platforms
            bcr.setParameter(765, 0); // For QC/MTK platforms
            bcr.setParameter(764, 3);
            //bcr.setParameter(137, 0);
            //bcr.setParameter(8610, 1);
//			 Parameters  param = bcr.getParameters();
//			 Log.d("012", "scan_h:"+bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES)+", cam_h:"+param.getPreviewSize().height);
//			 if(bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES) != param.getPreviewSize().height){
//				 param.setPreviewSize(1360, bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES));
//				 param.setPreviewSize(1360,960);
//				 bcr.setParameters(param);
//			 }

            // Sample of how to setup OCR Related String Parameters
            // OCR Parameters
            // Enable OCR-B
            //bcr.setParameter(681, 1);

            // Set OCR templates
            //String OCRSubSetString = "01234567890"; // Only numeric characters
            //String OCRSubSetString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!%"; // Only numeric characters
            // Parameter # 686 - OCR Subset
            //bcr.setParameter(686, OCRSubSetString);

            //String OCRTemplate = "54R"; // The D ignores all characters after the template
            // Parameter # 547 - OCR Template
            //bcr.setParameter(547, OCRTemplate);
            // Parameter # 689 - OCR Minimum characters
            //bcr.setParameter(689, 13);
            // Parameter # 690 - OCR Maximum characters
            //bcr.setParameter(690, 13);

            // Set Orientation
            bcr.setParameter(687, 4); // 4 - omnidirectional

            // Sets OCR lines to decide
            //bcr.setParameter(691, 2); // 2 - OCR 2 lines

            // End of OCR Parameter Sample
//			BarCodeReader.ReaderInfo readinfo = new BarCodeReader.ReaderInfo();
//			BarCodeReader.getReaderInfo(0, readinfo);
//			Log.e("012", "face:"+readinfo.facing);
        } catch (Exception e) {
            logger.e(TAG, "open exception " + e.getLocalizedMessage());
//            dspErr("open excp:" + e);
            Toast.makeText(context, "Open Exception " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void releaseResources() {
        if (bcr != null) {
            setIdle();
            bcr.release();
            bcr = null;
        }
    }

    // BarCodeReader.DecodeCallback override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        if (state == STATE_DECODE)
            state = STATE_IDLE;

        // Get the decode count
        if (length == BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT)
            decCount = symbology;

        if (length > 0) {
            if (isHandsFree() == false && isAutoAim() == false)
                bcr.stopDecode();

            ++decodes;

            if (symbology == 0x69)    // signature capture
            {
                if (sigcapImage) {
                    Bitmap bmSig = null;
                    int scHdr = 6;
                    if (length > scHdr)
                        bmSig = BitmapFactory.decodeByteArray(data, scHdr, length - scHdr);

//                    if (bmSig != null)
//                        snapScreen(bmSig);
//
//                    else
//                        dspErr("OnDecodeComplete: SigCap no bitmap");
                }
                decodeStatString += new String("[" + decodes + "] type: " + symbology + " len: " + length);
                decodeDataString += new String(data);

                mBarcodeCount++;
                long consum = System.currentTimeMillis() - mStartTime;
                mConsumTime += consum;
                decodeDataString += "\n\r" + "本次消耗时间:" + consum + "毫秒" + "\n\r" + "平均速度:" + (mConsumTime / mBarcodeCount) + "毫秒/个";
				/*try {
					decodeDataString += new String(data,charsetName(data));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
            } else {


                if (symbology == 0x99)    //type 99?
                {
                    symbology = data[0];
                    int n = data[1];
                    int s = 2;
                    int d = 0;
                    int len = 0;
                    byte d99[] = new byte[data.length];
                    for (int i = 0; i < n; ++i) {
                        s += 2;
                        len = data[s++];
                        System.arraycopy(data, s, d99, d, len);
                        s += len;
                        d += len;
                    }
                    d99[d] = 0;
                    data = d99;
                }

                Log.d("012", "ret=" + byte2hex(data));
                decodeStatString += new String("[" + decodes + "] type: " + symbology + " len: " + length);
                decodeDataString += new String(data);
                //add for test speed
                mBarcodeCount++;
                long consum = System.currentTimeMillis() - mStartTime;
                mConsumTime += consum;
                decodeDataString += "\n\r" + "本次消耗时间:" + consum + "毫秒" + "\n\r" + "平均速度:" + (mConsumTime / mBarcodeCount) + "毫秒/个";
				/*try {
					decodeDataString += new String(data,charsetName(data));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
//                dspStat(decodeStatString);
//                dspData(decodeDataString);

                logger.i(TAG, "Status " + decodeStatString);
                logger.i(TAG, "Data " + decodeDataString);

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Barcode barcode = new Barcode();
                            barcode.setName(decodeDataString);
                            listener.onScanSuccess(barcode);
                            listener.onScanningStatus(false);
                        }
                    });
                }

                if (decCount > 1) // Add the next line only if multiple decode
                {
                    decodeStatString += new String(" ; ");
                    decodeDataString += new String(" ; ");
                } else {
                    decodeDataString = new String("");
                    decodeStatString = new String("");
                }
            }

            if (beepMode)
                beep();
        } else    // no-decode
        {
//            dspData("");
            logger.i(TAG, "decoded is empty");
            switch (length) {
                case BarCodeReader.DECODE_STATUS_TIMEOUT:
//                    dspStat("decode timed out");
                    logger.e(TAG, "decode timed out");
                    break;

                case BarCodeReader.DECODE_STATUS_CANCELED:
//                    dspStat("decode cancelled");
                    logger.e(TAG, "decode cancelled");
                    break;

                case BarCodeReader.DECODE_STATUS_ERROR:
                default:
//                    dspStat("decode failed");
                    logger.e(TAG, "decode failed");
//      		Log.d("012", "decode failed length= " + length);
                    break;
            }
        }
//        mKeyF4Down = false;

        //}
    }

    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
        switch (event) {
            case BarCodeReader.BCRDR_EVENT_SCAN_MODE_CHANGED:
                ++modechgEvents;
                logger.i(TAG, "Scan Mode Changed Event (#" + modechgEvents + ")");
//                dspStat("Scan Mode Changed Event (#" + modechgEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_MOTION_DETECTED:
                ++motionEvents;
                logger.i(TAG, "Motion Detect Event (#" + motionEvents + ")");
//                dspStat("Motion Detect Event (#" + motionEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_SCANNER_RESET:
                logger.i(TAG, "Reset Event");
                //dspStat("Reset Event");
                break;

            default:
                // process any other events here
                break;
        }
//        mKeyF4Down = false;
    }

    @Override
    public void onError(int error, BarCodeReader reader) {

    }


    //    util methods
    private void beep() {
        BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
//		if (tg != null)
//			tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
    }

    private String byte2hex(byte[] buffer) {
        String h = "";

        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }

        return h;
    }

    // ==== SDL methods =====================

    // ----------------------------------------
    private boolean isHandsFree() {
        return (trigMode == BarCodeReader.ParamVal.HANDSFREE);
    }

    // ----------------------------------------
    private boolean isAutoAim() {
        return (trigMode == BarCodeReader.ParamVal.AUTO_AIM);
    }

    // ----------------------------------------
    // reset Level trigger mode
    void resetTrigger() {
        doSetParam(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
        trigMode = BarCodeReader.ParamVal.LEVEL;
    }

    // ----------------------------------------
    // get param
    private int doGetParam(int num) {
        int val = bcr.getNumParameter(num);
        if (val != BarCodeReader.BCR_ERROR) {
//            dspStat("Get # " + num + " = " + val);
//            edPval.setText(Integer.toString(val));
        } else {
//            dspStat("Get # " + num + " FAILED (" + val + ")");
//            edPval.setText(Integer.toString(val));
        }
        return val;
    }

    // ----------------------------------------
    // set param
    private int doSetParam(int num, int val) {
        String s = "";
        int ret = bcr.setParameter(num, val);
        if (ret != BarCodeReader.BCR_ERROR) {
            if (num == BarCodeReader.ParamNum.PRIM_TRIG_MODE) {
                trigMode = val;
                if (val == BarCodeReader.ParamVal.HANDSFREE) {
                    s = "HandsFree";
                } else if (val == BarCodeReader.ParamVal.AUTO_AIM) {
                    s = "AutoAim";
                    ret = bcr.startHandsFreeDecode(BarCodeReader.ParamVal.AUTO_AIM);
                    if (ret != BarCodeReader.BCR_SUCCESS) {
//                        dspErr("AUtoAIm start FAILED");
                        logger.e(TAG, "AUtoAIm start FAILED");
                    }
                } else if (val == BarCodeReader.ParamVal.LEVEL) {
                    s = "Level";
                }
            } else if (num == BarCodeReader.ParamNum.IMG_VIDEOVF) {
                if (snapPreview = (val == 1))
                    s = "SnapPreview";
            }
        } else
            s = " FAILED (" + ret + ")";

//        dspStat("Set #" + num + " to " + val + " " + s);
        return ret;
    }

    private int setIdle() {
        int prevState = state;
        int ret = prevState;        //for states taking time to chg/end

        state = STATE_IDLE;
        switch (prevState) {
            case STATE_HANDSFREE:
                resetTrigger();
                //fall thru
            case STATE_DECODE:
//                dspStat("decode stopped");
                logger.e(TAG, "decode stopped");
                Toast.makeText(context, "decode stopped", Toast.LENGTH_SHORT).show();
                bcr.stopDecode();
                break;

            case STATE_VIDEO:
                bcr.stopPreview();
                break;

            case STATE_SNAPSHOT:
                ret = STATE_IDLE;
                break;

            default:
                ret = STATE_IDLE;
        }
        return ret;
    }

    public void setOnBarcodeReaderListener(BarcodeReaderListener barcodeReaderListener) {
        this.listener = barcodeReaderListener;
    }
}
