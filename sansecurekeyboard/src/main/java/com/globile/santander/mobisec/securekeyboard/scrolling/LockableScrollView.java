package com.globile.santander.mobisec.securekeyboard.scrolling;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * Custom implementation of a NestedScrollingView with controls to enable or disable scrolling behaviour
 */
class LockableScrollView extends NestedScrollView {

    // ATTRIBUTES
    // ***********************************************************************

    private boolean enableScrolling = true;


    // METHODS
    // ***********************************************************************

    public LockableScrollView(@NonNull Context context) {
        super(context);
    }

    public LockableScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // GETTERS & SETTERS
    // ***********************************************************************

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }


    // OVERRIDE METHODS
    // ***********************************************************************

    /**
     * Intercepts touch event to check if scrolling movement is allowed
     * @param ev Motion event on this LockableScrollView
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isEnableScrolling()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (isEnableScrolling()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }

    }
}
