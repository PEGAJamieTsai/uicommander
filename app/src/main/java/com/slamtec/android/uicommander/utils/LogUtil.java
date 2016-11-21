package com.slamtec.android.uicommander.utils;

import android.util.Log;

import com.slamtec.android.uicommander.BuildConfig;

/**
 * Created by Alan on 10/16/15.
 */
public class LogUtil {

    private final static boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg, Exception e) {
        if (DEBUG) {
            Log.i(tag, msg, e);
        }
    }

    public static void d(String tag, String msg, Exception e) {
        if (DEBUG) {
            Log.d(tag, msg, e);
        }
    }

    public static void w(String tag, String msg, Exception e) {
        if (DEBUG) {
            Log.w(tag, msg, e);
        }
    }

    public static void e(String tag, String msg, Exception e) {
        if (DEBUG) {
            Log.e(tag, msg, e);
        }
    }
}
