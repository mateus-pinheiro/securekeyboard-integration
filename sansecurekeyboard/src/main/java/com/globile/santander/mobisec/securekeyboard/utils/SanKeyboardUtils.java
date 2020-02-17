package com.globile.santander.mobisec.securekeyboard.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;
import com.globile.santander.mobisec.securekeyboard.listeners.SanEventReceiver;

import java.util.Locale;

public abstract class SanKeyboardUtils {

	private SanKeyboardUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static final int INVALID_INDEX = Integer.MIN_VALUE;
	
	/**
	 * @param activity {@link Activity}.
	 *
	 * @return A {@link SanKeyboardView} object if its instance is already rendered on the window, or <b>null</b> if:<br>
	 * - the activity is null.<br>
	 * - the keyboard view is missing from the activity {@link android.view.Window window}.
	 */
	@Nullable
	public static SanKeyboardView findKeyboardView(Activity activity) {
		if (activity != null) {
			return findKeyboardView(activity.getWindow().getDecorView());
		}
		return null;
	}
	
	/**
	 * @param view Any {@link View} rendered on the window.
	 *
	 * @return A {@link SanKeyboardView} object if its instance is already rendered on the window, or <b>null</b> if:<br>
	 * - the view is null.<br>
	 * - the keyboard view is missing from the activity {@link android.view.Window window}.
	 */
	@Nullable
	private static SanKeyboardView findKeyboardView(View view) {
		if (view != null) {
			return view.getRootView().findViewById(R.id.san_keyboard_view);
		}
		return null;
	}
	
	/**
	 * @param view Any {@link View} rendered on the window.
	 *
	 * @return The keyboard view's singleton instance for the current activity window.
	 */
	@NonNull
	public static SanKeyboardView createKeyboardView(@NonNull View view) {

		SanKeyboardView sanKeyboardView = findKeyboardView(view);
		
		if (sanKeyboardView == null) {
			// There's no instance of our keyboard on the window, so we create and attach it.
			Context context = view.getContext();
			ViewGroup rootLayout = view.getRootView().findViewById(android.R.id.content);
			LayoutInflater.from(context).inflate(R.layout.san_keyboard_view, rootLayout);
			sanKeyboardView = findKeyboardView(view);
			
			if (sanKeyboardView == null) {
				throw new IllegalStateException("Error creating keyboard!");
			}

			sanKeyboardView.initAnimationsManager();
			
			context.sendBroadcast(new Intent(SanEventReceiver.ACTION_KEYBOARD_READY));
		}
		
		return sanKeyboardView;

	}
	
	public static Context getContextForLocale(Context context, Locale desiredLocale) {
		Configuration conf = new Configuration(context.getResources().getConfiguration());
		Context contextForLocale;
		if (Build.VERSION.SDK_INT >= 17) {
			conf.setLocale(desiredLocale);
			contextForLocale = context.createConfigurationContext(conf);
			contextForLocale.getResources().getConfiguration().setLocale(desiredLocale);//Needed, previous line is not enough sometimes
		} else {
			conf.locale = desiredLocale;
			context.getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
			contextForLocale = context;
		}
		return contextForLocale;
	}
	
}
