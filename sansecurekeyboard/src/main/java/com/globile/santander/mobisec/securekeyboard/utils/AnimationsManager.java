package com.globile.santander.mobisec.securekeyboard.utils;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;
import com.globile.santander.mobisec.securekeyboard.listeners.SanEventCallbacks;

public class AnimationsManager {

    private final int screenHeight;
    private final int slideInDurationMillis;
    private final int slideOutDurationMillis;
    private final int contentExpandDelayMillis;

    private int keyboardHeight;
    private Animation slideInAnimation;
    private Animation slideOutAnimation;
    private ValueAnimator expandContentAnimator;
    private ValueAnimator shrinkContentAnimator;

    public AnimationsManager(@NonNull SanKeyboardView keyboardView) {
        Resources resources = keyboardView.getResources();

        screenHeight = resources.getDisplayMetrics().heightPixels;
        slideInDurationMillis = resources.getInteger(android.R.integer.config_shortAnimTime);
        slideOutDurationMillis = resources.getInteger(android.R.integer.config_shortAnimTime);
        contentExpandDelayMillis = resources.getInteger(android.R.integer.config_shortAnimTime);
    
        initKeyboardRelatedAnimations(keyboardView);
    }

    public Animation getSlideInAnimation() {
        return slideInAnimation;
    }

    public Animation getSlideOutAnimation() {
        return slideOutAnimation;
    }

    private void initKeyboardRelatedAnimations(final SanKeyboardView keyboardView) {

        ViewGroup keyboardParent = (ViewGroup) keyboardView.getParent();
        int keyboardIndexInParent = keyboardParent.indexOfChild(keyboardView);

        if (keyboardIndexInParent == 0) {
            keyboardIndexInParent++;
        }

        final View contentView = keyboardParent.getChildAt(keyboardIndexInParent - 1);
        final int originalContentHeight = contentView.getMeasuredHeight();

        final ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //int val = (Integer) animation.getAnimatedValue();
                int val = -1;
                ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
                layoutParams.height = val;
                contentView.setLayoutParams(layoutParams);
            }
        };

        slideInAnimation = AnimationUtils.loadAnimation(keyboardView.getContext(), R.anim.slide_in);
        slideInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                keyboardView.setVisibility(View.VISIBLE);

                if (keyboardHeight == 0) {
                    keyboardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    keyboardHeight = keyboardView.getMeasuredHeight();
                }

                if (originalContentHeight <= screenHeight - keyboardHeight) {
                    return;
                }

                if (shrinkContentAnimator == null) {
                    shrinkContentAnimator = ValueAnimator.ofInt(originalContentHeight, originalContentHeight - keyboardHeight);
                    shrinkContentAnimator.setDuration(slideInDurationMillis);
                    shrinkContentAnimator.addUpdateListener(animatorUpdateListener);
                }
                shrinkContentAnimator.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SanEventCallbacks eventListener = keyboardView.getEventListener();
                if (eventListener != null) {
                    eventListener.onSanKeyboardShown(keyboardView);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Nothing to do
            }
        });

        slideOutAnimation = AnimationUtils.loadAnimation(keyboardView.getContext(), R.anim.slide_out);
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (keyboardHeight == 0) {
                    keyboardHeight = keyboardView.getMeasuredHeight();
                }

                if (originalContentHeight <= screenHeight - keyboardHeight) {
                    return;
                }

                if (expandContentAnimator == null) {
                    expandContentAnimator = ValueAnimator.ofInt(contentView.getMeasuredHeight(), originalContentHeight);
                    expandContentAnimator.setDuration(slideOutDurationMillis + contentExpandDelayMillis);
                    expandContentAnimator.addUpdateListener(animatorUpdateListener);
                }
                expandContentAnimator.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                keyboardView.setVisibility(View.GONE);

                SanEventCallbacks eventListener = keyboardView.getEventListener();
                if (eventListener != null) {
                    eventListener.onSanKeyboardHidden(keyboardView);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Nothing to do
            }
        });
    }

}