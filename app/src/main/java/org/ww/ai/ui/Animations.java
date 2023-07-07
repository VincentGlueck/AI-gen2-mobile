package org.ww.ai.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public enum Animations {
    ANIMATIONS;

    public Animation getScaleAnimation(float from, float to, long animationTime, boolean... once) {
        Animation anim = new ScaleAnimation(
                from, to, // Start and end values for the X axis scaling
                from, to, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(animationTime);
        if(once.length > 0 && once[0]) {
            if(to > from) {
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                anim.setInterpolator(new DecelerateInterpolator());
            }
            anim.setRepeatCount(0);
            anim.setFillAfter(true);
        } else {
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
        }
        return anim;
    }

    public Animation getRotateAnimation(float from, float to, long animationTime, boolean... once) {
        RotateAnimation anim = new RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(animationTime);
        anim.setStartOffset(0);
        if(once.length > 0 && once[0]) {
            anim.setRepeatCount(0);
            anim.setFillAfter(true);
        } else {
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
        }
        return anim;
    }

    public Animation getAlphaAnimation(float from, float to, long animationTime, boolean... once) {
        Animation anim = new AlphaAnimation(from, to);
        anim.setDuration(animationTime); //You can manage the time of the blink with this parameter
        anim.setStartOffset(0);
        if(once.length > 0 && once[0]) {
            anim.setRepeatCount(0);
            anim.setFillAfter(true);
        } else {
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
        }
        return anim;
    }

    public Animation getTranslateAnimation(int fromXDelta, int toXDelta, int fromYDelta, int toYDelta, int duration, boolean... once) {
        Animation anim = new TranslateAnimation(fromXDelta, toXDelta,fromYDelta, toYDelta);
        anim.setDuration(duration);
        if(once.length > 0 && once[0]) {
            anim.setRepeatCount(0);
            anim.setFillAfter(true);
        } else {
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
        }
        return anim;
    }

    public void animateVerticalSize(View view, int from, int to) {
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = val;
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(400);
        anim.start();
    }

    public float convertDpToPixel(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
