package com.globile.santander.mobisec.securekeyboard.enums;

import androidx.annotation.DrawableRes;

import com.globile.santander.mobisec.securekeyboard.R;

import java.util.Arrays;

/**
 * ShiftMode is an enum which contains keyboard's possible letter case formats.
 */
public enum ShiftMode {

    LOWER_CASE(R.drawable.key_shift_default),
    UPPER_CASE_SINGLE(R.drawable.key_shift_upper),
    UPPER_CASE_CONTINUOUS(R.drawable.key_shift_upper_perm);

    private final int drawableResId;

    ShiftMode(@DrawableRes int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public ShiftMode getNext() {
        int index = Arrays.binarySearch(values(), this);
        return index < values().length - 1 ? values()[index + 1] : values()[0];
    }

}