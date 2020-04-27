package com.example.chekersgamepro.views.custom;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.example.chekersgamepro.R;

public class ColorAnimateView extends AppCompatTextView implements TimeAnimator.TimeListener{

    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;

    private static int LEVEL_INCREMENT = 1500;
    private static final int MAX_LEVEL = 10000;

    private ClipDrawable mClipDrawable;

    public ColorAnimateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set up TimeAnimator to fire off on button click.
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);

    }

    public void animateUnselectedRightToLeft(){

        animateAndPerformClick(R.id.clip_drawable_unselected_right_left, R.drawable.button_avatar_picker_background_unselected_right_left);

    }

    public void animateSelectedRightToLeft(){
        animateAndPerformClick(R.id.clip_drawable_selected_right_left, R.drawable.button_avatar_picker_background_selected_right_left);
    }

    public void animateUnselectedLeftToRight(){
        animateAndPerformClick(R.id.clip_drawable_unselected_left_right, R.drawable.button_avatar_picker_background_unselected_left_right);

    }

    public void animateSelectedLeftToRight(){
        animateAndPerformClick(R.id.clip_drawable_selected_left_right, R.drawable.button_avatar_picker_background_selected_left_right);
    }

    private void animateAndPerformClick(int animateDrawableId, int drawableId){
        setBackgroundDrawable(drawableId);
        // Get a handle on the ClipDrawable that we will animate.
        LayerDrawable layerDrawable = (LayerDrawable) getBackground();
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(animateDrawableId);
        animateButton();
    }

    public void setBackgroundDrawable(int drawableId){
        setBackground(getContext().getDrawable(drawableId));
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mClipDrawable.setLevel(mCurrentLevel);

        if (mCurrentLevel >= MAX_LEVEL) {
            mAnimator.cancel();
        } else {
            mCurrentLevel = Math.min(MAX_LEVEL, mCurrentLevel + LEVEL_INCREMENT);
        }
    }

    public void animateButton() {

        if (!mAnimator.isRunning()) {
            mCurrentLevel = 0;
            mAnimator.start();
        }
    }

}
