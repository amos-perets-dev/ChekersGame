package com.example.chekersgamepro.views.custom.circle;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import org.jetbrains.annotations.Nullable;

public class CircleAngleAnimation extends Animation {

    private CircleImageViewCustom circle;

    private float oldAngle;
    private float newAngle;

    public CircleAngleAnimation( CircleImageViewCustom circle) {
        this.oldAngle = circle.getAngle();
        this.circle = circle;
        setDuration(circle.getDrawAnimationDuration());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        circle.setAngle(angle);
        circle.requestLayout();
    }

    public void setAngle(boolean isSelected) {
        this.newAngle = isSelected ? 360 : 0;
    }

}