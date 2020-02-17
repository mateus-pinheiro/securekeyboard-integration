package com.globile.santander.mobisec.logger;

import android.util.Log;

public class GlobileLog {
    
    private static boolean enabled;
    
    public GlobileLog() {
        throw new IllegalStateException("Static Logger class");
    }
    
    public static void enable() {
        enabled = true;
    }
    
    // VERBOSE
    public static void v(String msg) {
        if (enabled) {
            String tag = buildTag();
            Log.v(tag, msg);
        }
    }
    
    public static void v(String tag, String msg) {
        if (enabled) {
            Log.v(tag, msg);
        }
    }
    
    public static void v(String tag, String msg, Throwable tr) {
        if (enabled) {
            Log.v(tag, msg, tr);
        }
    }
    
    // DEBUG
    public static void d(String msg) {
        if (enabled) {
            String tag = buildTag();
            GlobileLog.d(tag, msg);
        }
    }
    
    public static void d(String tag, String msg) {
        if (enabled) {
            Log.d(tag, msg);
        }
    }
    
    public static void d(String tag, String msg, Throwable tr) {
        if (enabled) {
            Log.d(tag, msg, tr);
        }
    }
    
    
    // INFO
    public static void i(String msg) {
        if (enabled) {
            String tag = buildTag();
            GlobileLog.i(tag, msg);
        }
    }
    
    public static void i(String tag, String msg) {
        if (enabled) {
            Log.i(tag, msg);
        }
    }
    
    public static void i(String tag, String msg, Throwable tr) {
        if (enabled) {
            Log.i(tag, msg, tr);
        }
    }
    
    // ERROR
    public static void e(Throwable tr) {
        if (enabled) {
            String tag = buildTag();
            Log.e(tag, "", tr);
        }
    }
    
    public static void e(String msg) {
        if (enabled) {
            String tag = buildTag();
            Log.e(tag, msg);
        }
    }
    
    public static void e(String msg, Throwable tr) {
        if (enabled) {
            String tag = buildTag();
            Log.e(tag, msg, tr);
        }
    }
    
    private static String buildTag() {
        try {
            return Thread.currentThread().getStackTrace()[4].getFileName().split("\\.")[0]
                    + "."
                    + Thread.currentThread().getStackTrace()[4].getMethodName()
                    + ":"
                    + Thread.currentThread().getStackTrace()[4].getLineNumber();
        } catch (Throwable e) {
            return "GlobileLog";
        }
    }
}
