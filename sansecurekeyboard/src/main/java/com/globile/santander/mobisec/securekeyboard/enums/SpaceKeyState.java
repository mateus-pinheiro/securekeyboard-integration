package com.globile.santander.mobisec.securekeyboard.enums;

import androidx.annotation.DrawableRes;

import com.globile.santander.mobisec.securekeyboard.R;

import java.util.Arrays;

/**
 * SpaceKeyState is an enum which contains keyboard's possible SpaceKey states.
 */
public enum SpaceKeyState {

    NORMAL(R.drawable.keyboard_key_space_background_normal),
    PRESSED(R.drawable.keyboard_key_space_background_pressed);

    private final int drawableResId;

    SpaceKeyState(@DrawableRes int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public int getIndex() {
        return Arrays.binarySearch(values(), this);
    }

}