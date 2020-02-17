package com.globile.santander.mobisec.securekeyboard.listeners;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

/**
 * This BroadcastReceiver needs to be registered inside an Activity, otherwise it could malfunction.
 */
public class SanEventReceiver extends BroadcastReceiver {

    public static final String ACTION_KEYBOARD_READY = SanEventReceiver.class.getName() + ".ACTION_KEYBOARD_READY";

    private final WeakReference<SanEventCallbacks> sanEventCallbacksWeakRef;

    public SanEventReceiver(SanEventCallbacks sanEventCallbacks) {
        this.sanEventCallbacksWeakRef = new WeakReference<>(sanEventCallbacks);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        if (ACTION_KEYBOARD_READY.equals(intent.getAction())) {
            SanEventCallbacks sanEventCallbacks = sanEventCallbacksWeakRef.get();
            if (sanEventCallbacks != null && context instanceof Activity) {
                SanKeyboardView sanKeyboardView = SanKeyboardUtils.findKeyboardView((Activity) context);
                sanEventCallbacks.onSanKeyboardReady(sanKeyboardView);
            }
        }

    }

}
