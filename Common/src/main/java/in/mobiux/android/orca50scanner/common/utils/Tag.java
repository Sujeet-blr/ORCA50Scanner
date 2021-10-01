package in.mobiux.android.orca50scanner.common.utils;

public class Tag {

    private static final boolean FINAL_CONSTANT_IS_LOCAL = true;
    private static String TAG = Tag.class.getSimpleName();

    public Tag(String tag) {
        TAG = tag;
    }

    private String getLogTagWithMethod() {
        if (FINAL_CONSTANT_IS_LOCAL) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            return trace[0].getClassName() + "." + trace[0].getMethodName() + ":" + trace[0].getLineNumber();
        } else {
            return TAG;
        }
    }
}
