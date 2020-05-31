package com.example.chekersgamepro.views.custom;

public interface IBaseViewAnimationAngle {

    default boolean isAnimationViewAngle(){
        return false;
    }

    void setAngle(float angle);

    void requestLayoutNow();

    long getDrawAnimationDuration();

    float getAngle();

    void setCircleColor(int color);

    void setMarginDrawCircle(int dp);

    void setCircleWidth(float strokeWidth);

    void setDurationAnimation(long duration);
}
