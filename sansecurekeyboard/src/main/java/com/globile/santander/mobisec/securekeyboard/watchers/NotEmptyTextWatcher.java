package com.globile.santander.mobisec.securekeyboard.watchers;

import com.globile.santander.mobisec.securekeyboard.BaseSecureEditText;

import java.lang.ref.WeakReference;

public class NotEmptyTextWatcher extends SecureTextWatcher {

    private final int keycode;
	private final WeakReference<BaseSecureEditText> weakSecureEditText;

	public NotEmptyTextWatcher(int keycode, BaseSecureEditText sanEditText) {

		this.keycode = keycode;
		this.weakSecureEditText = new WeakReference<>(sanEditText);

	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		if (weakSecureEditText.get() != null) {
			weakSecureEditText.get().enableDisableCustomKey(keycode, s.length() > 0);
		}

    }

}
