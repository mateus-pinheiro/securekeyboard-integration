package com.globile.santander.mobisec.securekeyboard.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.enums.InputLanguage;
import com.globile.santander.mobisec.securekeyboard.enums.SpaceKeyState;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager;

import java.util.EnumMap;
import java.util.Map;

public class SpaceKeyStateManager {

	private final Map<InputLanguage, Drawable[]> spaceStateDrawables = new EnumMap<>(InputLanguage.class);
	private final LayoutInflater inflater;
	
	public SpaceKeyStateManager(@NonNull Context context, int keyWidth, int keyHeight) {
		inflater = LayoutInflater.from(context);
		initDrawablesList(context.getResources(), keyWidth, keyHeight);
	}
	
	public Drawable getDrawableForState(InputLanguage inputLanguage, SpaceKeyState state) {
		return spaceStateDrawables.get(inputLanguage)[state.getIndex()];
	}
	
	private void initDrawablesList(Resources resources, int keyWidth, int keyHeight) {
		int statesCount = SpaceKeyState.values().length;
		
		for (InputLanguage inputLanguage : SanKeyboardManager.getPossibleLanguages()) {
			Drawable[] drawables = new Drawable[statesCount];
			for (int i = 0; i < statesCount; i++) {
				drawables[i] = generateDrawable(resources, keyWidth, keyHeight, inputLanguage, SpaceKeyState.values()[i]);
			}
			spaceStateDrawables.put(inputLanguage, drawables);
		}
	}
	
	private Drawable generateDrawable(Resources resources, int keyWidth, int keyHeight, InputLanguage inputLanguage, SpaceKeyState state) {
		View spaceView = inflater.inflate(R.layout.space_view, null);
		
		spaceView.findViewById(R.id.space_main_layout)
				.setBackgroundResource(state.getDrawableResId());
		
		if (SanKeyboardManager.getPossibleLanguages().length > 1) {
			((TextView) spaceView.findViewById(R.id.space_layout_name))
					.setText(inputLanguage.getText(resources));//Multilingual
		} else {//No other languages to change to
			spaceView.findViewById(R.id.space_layout_name).setVisibility(View.INVISIBLE);
			spaceView.findViewById(R.id.space_arrow_left).setVisibility(View.INVISIBLE);
			spaceView.findViewById(R.id.space_arrow_right).setVisibility(View.INVISIBLE);
		}
		
		spaceView.setDrawingCacheEnabled(true);
		spaceView.measure(keyWidth, keyHeight);
		spaceView.layout(0, 0, keyWidth, keyHeight);
		spaceView.buildDrawingCache(true);
		
		Bitmap viewBitmap = Bitmap.createBitmap(spaceView.getDrawingCache());
		spaceView.setDrawingCacheEnabled(false);
		
		return new BitmapDrawable(resources, viewBitmap);
	}
	
}
