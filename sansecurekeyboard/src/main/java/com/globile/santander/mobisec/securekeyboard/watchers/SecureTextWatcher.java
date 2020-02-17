package com.globile.santander.mobisec.securekeyboard.watchers;

import android.text.Editable;
import android.text.TextWatcher;

import com.globile.santander.mobisec.securekeyboard.SanEditText;

/**
 * Enables the Continue button if the {@link SanEditText} is not empty; disables it otherwise.
 */
public abstract class SecureTextWatcher implements TextWatcher {
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	
	}
	
	@Override
	public void afterTextChanged(Editable s) {
	}
}