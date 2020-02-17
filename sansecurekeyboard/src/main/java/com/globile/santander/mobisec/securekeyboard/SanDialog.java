package com.globile.santander.mobisec.securekeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.globile.santander.mobisec.securekeyboard.listeners.SanEventCallbacks;
import com.globile.santander.mobisec.securekeyboard.listeners.SanEventReceiver;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

import java.lang.ref.WeakReference;

/**
 * If you need the SanEditText on a Dialog, please use the custom SanDialog and add the SanEditText
 * to its layout.
 */
public class SanDialog extends AppCompatActivity implements SanEventCallbacks {

    private static final int LAYOUT_RES_DEFAULT_VALUE = 0;

    private static int contentResId;
    private static WeakReference<Resources.Theme> contentThemeWeakRef;
    private static WeakReference<View> contentViewWeakRef;

    public static void show(@NonNull Context context, @LayoutRes int layoutResId) {
        contentResId = layoutResId;
        contentThemeWeakRef = new WeakReference<>(context.getTheme());
        context.startActivity(new Intent(context, SanDialog.class));
    }

    public static void show(@NonNull View view) {
        contentViewWeakRef = new WeakReference<>(view);
        view.getContext().startActivity(new Intent(view.getContext(), SanDialog.class));
    }

    private Resources.Theme extraTheme;
    private View extraView;

    private ViewGroup baseLayout;

    private SanEventReceiver sanEventReceiver;
    private IntentFilter sanEventFilter;

    /**
     * The View object holds a reference to the Context. Because we keep the View static,
     * it's best to release it immediately to avoid leaking the Context.
     *
     * Call this method at the earliest possible moment
     * to prevent app crashes from leaving these values in memory.
     */
    private void retrieveAndReleaseStaticExtras() {
        if (contentThemeWeakRef != null) {
            extraTheme = contentThemeWeakRef.get();
            contentThemeWeakRef = null;
        }

        if (contentViewWeakRef != null) {
            extraView = contentViewWeakRef.get();
            contentViewWeakRef = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveAndReleaseStaticExtras();

        setContentView(R.layout.san_dialog_view);
        addDialogContent();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SanKeyboardView sanKeyboardView = SanKeyboardUtils.findKeyboardView(this);
        if (sanKeyboardView != null) {
            onSanKeyboardReady(sanKeyboardView);
        } else {
            if (sanEventReceiver == null) {
                sanEventReceiver = new SanEventReceiver(this);
                sanEventFilter = new IntentFilter(SanEventReceiver.ACTION_KEYBOARD_READY);
            }
            registerReceiver(sanEventReceiver, sanEventFilter);
        }
    }

    @Override
    protected void onStop() {
        terminateSanEventReceiver();
        super.onStop();
    }

    @Override
    public void onSanKeyboardReady(SanKeyboardView keyboardView) {
        terminateSanEventReceiver();
        keyboardView.setEventListener(this);
    }

    @Override
    public void onSanKeyboardShown(SanKeyboardView keyboardView) {

    }

    @Override
    public void onSanKeyboardHidden(SanKeyboardView keyboardView) {

    }

    @NonNull
    private ViewGroup getVerifiedBaseLayout() {
        if (baseLayout == null) {
            // This must be the layout to which the dialog content will be added.
            baseLayout = findViewById(R.id.san_dialog_content_view);
            if (baseLayout == null) {
                // Bad structure of the layout xml.
                throw new IllegalStateException("There was an error");
            }
        }

        return baseLayout;
    }

    /**
     * @throws RuntimeException indicates misuse of this class.
     *         Please verify the method and data used to start this dialog simulating Activity.
     */
    private void addDialogContent() throws RuntimeException {
        if (extraTheme != null) {
            try {
                LayoutInflater.from(this).inflate(contentResId, getVerifiedBaseLayout());
                applyBaseActivityTheme(extraTheme);
            } finally {
                contentResId = LAYOUT_RES_DEFAULT_VALUE;
            }

        } else if (extraView != null) {
            Resources.Theme theme = extraView.getContext().getTheme();
            getVerifiedBaseLayout().addView(extraView);
            applyBaseActivityTheme(theme);

        } else {
            throw new IllegalStateException("There was an error");
        }
    }

    private void applyBaseActivityTheme(Resources.Theme theme) {
        if (theme == null) {
            return;
        }

        View dialogContentView = getVerifiedBaseLayout().getChildAt(0);
        if (dialogContentView == null) {
            return;
        }

        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);

        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = typedValue.data;
            dialogContentView.setBackgroundColor(color);

        } else try {
            // windowBackground is not a color, probably a drawable
            Drawable drawable = getResources().getDrawable(typedValue.resourceId);
            dialogContentView.setBackground(drawable);
        } catch (Exception e) {
            // default white background
            dialogContentView.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
    }

    private void terminateSanEventReceiver() {
        if (sanEventReceiver != null) {
            unregisterReceiver(sanEventReceiver);
            sanEventReceiver = null;
        }
    }

}
