package com.leo618.librecorder;

import android.util.Log;

/**
 * function:日志打印工具
 *
 * <p>
 * Created by Leo on 2017/11/29.
 */
/*package*/ class RecorderLog {
    private static final String TAG = "Recorder";

    private static boolean DEBUG = true;

    static void debug(boolean debug) {
        DEBUG = debug;
    }

    static void d(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }

    static void e(String msg) {
        if (DEBUG) Log.e(TAG, msg);
    }
}
