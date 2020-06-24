package com.example.chekersgamepro;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_UP;

public class TouchClickListener implements View.OnTouchListener {

    /**
     * Click listener we want to wrap
     */
    private View.OnClickListener clickListener;

    /**
     * View's Rect
     */
    private Rect viewRect;

    /**
     * Animator to perform animations
     */
    protected ObjectAnimator animator;

    private static final float DEFAULT_DECREASE_FACTOR = 0.9f;
    private static final int DEFAULT_ANIMATION_DURATION = 200;
    private int animationDuration;
    private float decreaseFactor;

    private float releaseScale = 1f;
    private View viewToAnimate;
    private final Handler handler = new Handler();
    private boolean withDelay = false;

    public TouchClickListener(View.OnClickListener clickListener) {
        this(clickListener, DEFAULT_DECREASE_FACTOR, DEFAULT_ANIMATION_DURATION);
    }

    public TouchClickListener(View.OnClickListener clickListener, float decreaseFactor) {
        this(clickListener, decreaseFactor, DEFAULT_ANIMATION_DURATION);
    }

    public TouchClickListener(View.OnClickListener clickListener, float decreaseFactor, int animationDuration) {
        this.clickListener = clickListener;
        this.decreaseFactor = decreaseFactor;
        this.animationDuration = animationDuration;
    }

    public TouchClickListener setWithDelay(boolean withDelay) {
        this.withDelay = withDelay;
        return this;
    }


    public void setViewToAnimate(View viewToAnimate) {
        this.viewToAnimate = viewToAnimate;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ObjectAnimator getAnimator() {
        return animator;
    }

    public float getReleaseScale() {
        return releaseScale;
    }

    public TouchClickListener setReleaseScale(float releaseScale) {
        this.releaseScale = releaseScale;
        return this;
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN: {
                viewRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                final PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, decreaseFactor * getReleaseScale());
                final PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, decreaseFactor * getReleaseScale());
                if (animator == null) {
                    animator = ObjectAnimator.ofPropertyValuesHolder(viewToAnimate == null ? v : viewToAnimate, scaleX, scaleY);
                    animator.setInterpolator(new FastOutSlowInInterpolator());
                } else {
                    animator.setValues(scaleX, scaleY);
                }
                animator.setDuration(animationDuration);
                animator.start();
                return true;
            }
            case ACTION_CANCEL:
                // needed not to perform click
            case ACTION_UP:
                if (animator == null) {
                    return false;
                }
                final PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, getReleaseScale());
                final PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, getReleaseScale());
                animator.setValues(scaleX, scaleY);
                animator.setDuration(animationDuration);
                animator.start();

                if (event.getAction() != ACTION_CANCEL &&
                        clickListener != null &&
                        viewRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    handler.removeCallbacksAndMessages(null);

                    if (withDelay) {
                        handler.postDelayed(() -> clickListener.onClick(v), 300);
                    } else {
                        clickListener.onClick(v);
                    }
                }
                return true;
        }
        return false;
    }
}
