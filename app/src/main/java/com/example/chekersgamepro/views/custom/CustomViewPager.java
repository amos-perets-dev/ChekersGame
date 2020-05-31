package com.example.chekersgamepro.views.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private Boolean disable = false;

    public CustomViewPager(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !disable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !disable && super.onTouchEvent(event);
    }

    public void disableScroll(Boolean disable) {
        //When disable = true not work the scroll and when disble = false work the scroll
        this.disable = disable;
    }

    @Override
    protected void onAttachedToWindow() {
        Log.d("TEST_GAME", "CustomViewPager onAttachedToWindow: ");

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d("TEST_GAME", "CustomViewPager onDetachedFromWindow: ");

        super.onDetachedFromWindow();
    }
}