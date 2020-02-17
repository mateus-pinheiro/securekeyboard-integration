package com.globile.santander.mobisec.securekeyboard;

import android.content.Context;
import android.util.AttributeSet;

/**
 * SanEditText is the component that you need to use instead of the standard EditText on xml in
 * order to integrate SanKeyboard on any View.
 */
public class SanEditText extends BaseSecureEditText {

	public SanEditText(Context context) {
		super(context);
	}

	public SanEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SanEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

}
