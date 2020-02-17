package com.globile.santander.mobisec.securekeyboard.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardAOSP;
import com.globile.santander.mobisec.securekeyboard.enums.ShiftMode;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.utils.DrawableUtils;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Secure Keyboard's main object.
 *
 * NOTE: Fixed problem with getNearestKeys() not returning proper values for wide buttons.
 */
public class SanKeyboard extends KeyboardAOSP {

	// ATTRIBUTES
	// ***********************************************************************

	public static final int KEYCODE_SPECIAL_CHANGE = -7;
	public static final int KEYCODE_CONTINUE = -8; // Key code for continue action
	public static final int KEYCODE_SECURE_KEYBOARD = -9; // Key code for secure keyboard button
	public static final int KEYCODE_SPACE = 32;
	public static final int KEYCODE_DECIMAL_POINT = 46;

	private static final String CONTINUE_ONLY = "ContinueOnly";
	private static final String CONTINUE = "Continue";
	private static final String CANCEL = "Cancel";
	private static final String DONE = "Done";
	
	private List<Integer> keyCodesList;

	private ShiftMode initialShift;

	private List<Integer> topRowKeyCodesList;
	private TopRowButtonsOptions topRowButtonsSelected;

	private Map<String, SanCustomKeyData> sanCustomKeyDatas;

	private Context context;

	// CONSTRUCTORS
	// ***********************************************************************

	public SanKeyboard(@NonNull Context context, int xmlLayoutResId) {
		super(context, xmlLayoutResId);
		init(context);
	}
	
	public SanKeyboard(@NonNull Context context, int xmlLayoutResId, int modeId, int width, int height) {
		super(context, xmlLayoutResId, modeId, width, height);
		init(context);
	}
	
	public SanKeyboard(@NonNull Context context, int xmlLayoutResId, int modeId) {
		super(context, xmlLayoutResId, modeId);
		init(context);
	}
	
	public SanKeyboard(@NonNull Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
		super(context, layoutTemplateResId, characters, columns, horizontalPadding);
		init(context);
	}

	// GETTERS & SETTERS
	// ***********************************************************************

	public ShiftMode getInitialShift() {
		return initialShift;
	}

	public void setInitialShift(ShiftMode initialShift) {
		this.initialShift = initialShift;
	}

	public TopRowButtonsOptions getTopRowButtonsSelected() {
		return topRowButtonsSelected;
	}

	// METHODS
	// ***********************************************************************

	private void init(Context context) {

		this.context = context;

		generateCustomKeyData(context.getResources());

		initializeKeys();

	}

	private void initializeKeys() {

		List<Key> keys = getKeys();
		keyCodesList = new ArrayList<>(keys.size());
		topRowKeyCodesList = new ArrayList<>();
		int[] topRowKeys = context.getResources().getIntArray(R.array.topRowKeys);

		for (Key key : keys) {

			int keyCode = key.codes[0];
			keyCodesList.add(keyCode);

			// Detect if is top row
			for (int topRowKey : topRowKeys) {
				if (keyCode == topRowKey) {
					topRowKeyCodesList.add(key.codes[0]); //Count how many buttons are in the top row
				}
			}

			// Keys with custom drawable
			SanCustomKeyData sanCustomKeyData = getSanCustomKeyDataForKeyCode(keyCode);

			if (sanCustomKeyData != null) {
				enableDisableCustomKey(key.codes[0], true);
			}

		}

		switch (topRowKeyCodesList.size()) {

			default:
			case 0:
				topRowButtonsSelected = TopRowButtonsOptions.NONE;
				break;
			case 1:
				topRowButtonsSelected = TopRowButtonsOptions.CONTINUE_ONLY;
				break;
			case 2:
				topRowButtonsSelected = TopRowButtonsOptions.CANCEL_CONTINUE;
				break;

		}

	}

	@Override
	public int[] getNearestKeys(int x, int y) {

		List<Key> keys = getKeys();

		for (int i = 0; i < keys.size(); i++) {

			if (keys.get(i).isInside(x, y)) {
				return new int[] { i };
			}

		}

		return new int[0];

	}
	
	@Nullable
	public Key getKeyByCode(int keyCode) {

		int keyIndex = getKeyIndexByCode(keyCode);

		if (keyIndex == SanKeyboardUtils.INVALID_INDEX) {
			return null;
		}

		return getKeys().get(keyIndex);

	}
	
	/**
	 * @param keyCode The code from key to be searched
	 *
	 * @return The index of the key in the list of keys
	 * or {@value SanKeyboardUtils#INVALID_INDEX} if there's no matching key.
	 */
	public int getKeyIndexByCode(int keyCode) {
		int index = keyCodesList.indexOf(keyCode);
		return index >= 0 ? index : SanKeyboardUtils.INVALID_INDEX;
	}

	// CUSTOM KEY DATA
	// ***********************************************************************

	public void enableDisableCustomKey(int code, boolean enabled) {

		Key key = getKeyByCode(code);
		SanCustomKeyData sanCustomKeyData = getSanCustomKeyDataForKeyCode(code);

		if (sanCustomKeyData != null && key != null) {

			sanCustomKeyData.enabled = enabled;
			key.icon = generateCustomKeyDrawable(key, enabled);

		}

	}

	public boolean isKeyEnabled(int code) {
		SanCustomKeyData sanCustomKeyData = getSanCustomKeyDataForKeyCode(code);
		if (sanCustomKeyData != null) {
			return sanCustomKeyData.enabled;
		}
		return true; //No aplica activar o desactivar
	}

	@Nullable
	private Drawable generateCustomKeyDrawable(Key key, boolean enabled) {

		SanCustomKeyData sanCustomKeyData = getSanCustomKeyDataForKeyCode(key.codes[0]);

		if (sanCustomKeyData != null) {

			return DrawableUtils.generateTextViewCanvas(context, key.width, key.height, sanCustomKeyData.getBgColor(enabled),
					sanCustomKeyData.getTextColor(enabled), sanCustomKeyData.text, sanCustomKeyData.roundBorders);

		}

		return null;

	}

	@Nullable
	private SanCustomKeyData getSanCustomKeyDataForKeyCode(int code) {

		SanCustomKeyData sanCustomKeyData = null;

		switch (code) {

			case KEYCODE_CONTINUE:
				if (topRowKeyCodesList.size() == 1) {//If there is only the Continue button
					sanCustomKeyData = sanCustomKeyDatas.get(CONTINUE_ONLY);
				} else {
					sanCustomKeyData = sanCustomKeyDatas.get(CONTINUE);
				}
				break;

			case KEYCODE_CANCEL:
				sanCustomKeyData = sanCustomKeyDatas.get(CANCEL);
				break;

			case KEYCODE_DONE:
				sanCustomKeyData = sanCustomKeyDatas.get(DONE);
				break;

		}

		return sanCustomKeyData;

	}

	private void generateCustomKeyData(Resources res) {

		sanCustomKeyDatas = new TreeMap<>();

		sanCustomKeyDatas.put(CONTINUE_ONLY, new SanCustomKeyData(CONTINUE_ONLY, KEYCODE_CONTINUE,
				res.getColor(R.color.keyboard_sanred), Color.WHITE, //Enabled
				Color.TRANSPARENT, Color.GRAY, //Disabled
				res.getString(R.string.securekeyboard_continue), false));

		sanCustomKeyDatas.put(CONTINUE, new SanCustomKeyData(CONTINUE, KEYCODE_CONTINUE,
				Color.TRANSPARENT, res.getColor(R.color.keyboard_sanred), //Enabled
				Color.TRANSPARENT, Color.GRAY, //Disabled
				res.getString(R.string.securekeyboard_continue), false));

		sanCustomKeyDatas.put(CANCEL, new SanCustomKeyData(CANCEL, KEYCODE_CANCEL,
				Color.TRANSPARENT, Color.BLACK, //Enabled
				Color.TRANSPARENT, Color.GRAY, //Disabled
				res.getString(R.string.securekeyboard_cancel), false));

		sanCustomKeyDatas.put(DONE, new SanCustomKeyData(DONE, KEYCODE_DONE,
				res.getColor(R.color.keyboard_done_key_background), Color.WHITE,
				Color.GRAY, Color.WHITE,
				res.getString(R.string.securekeyboard_done), true));

	}


	private static class SanCustomKeyData {

		private final String id;
		private final int code;
		private final int enabledBgColor;
		private final int enabledTextColor;
		private final int disabledBgColor;
		private final int disabledTextColor;
		private final String text;
		private final boolean roundBorders;
		private boolean enabled = true; //Unless stated otherwise

		SanCustomKeyData(String id, int code, int enabledBgColor, int enabledTextColor, int disabledBgColor,
				int disabledTextColor, String text, boolean roundBorders) {
			this.id = id;
			this.code = code;
			this.enabledBgColor = enabledBgColor;
			this.disabledBgColor = disabledBgColor;
			this.enabledTextColor = enabledTextColor;
			this.disabledTextColor = disabledTextColor;
			this.text = text;
			this.roundBorders = roundBorders;
		}

		private int getBgColor(boolean enabled) {
			return enabled ? enabledBgColor : disabledBgColor;
		}

		private int getTextColor(boolean enabled) {
			return enabled ? enabledTextColor : disabledTextColor;
		}

	}


}
