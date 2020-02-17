package com.globile.santander.mobisec.securekeyboard.listeners;

import android.view.MotionEvent;

public interface SanTapJackedCallback {
	
	/**
	 * @param event Motion event detected
	 *
	 * @return <b>True</b> if you want the touch event to be dispatched, <b>False</b> to block it.
	 */
	boolean onObscuredTouchEvent(MotionEvent event);
	
}
