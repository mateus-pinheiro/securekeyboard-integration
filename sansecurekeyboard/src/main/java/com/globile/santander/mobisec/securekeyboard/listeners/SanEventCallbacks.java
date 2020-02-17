package com.globile.santander.mobisec.securekeyboard.listeners;

import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;

/**
 * SanEventCallbacks interface is an interface which should be used for sending analytics
 * events to measure the usage and success of this component.
 */
public interface SanEventCallbacks {
	
	void onSanKeyboardReady(SanKeyboardView keyboardView);
	
	void onSanKeyboardShown(SanKeyboardView keyboardView);
	
	void onSanKeyboardHidden(SanKeyboardView keyboardView);
	
}