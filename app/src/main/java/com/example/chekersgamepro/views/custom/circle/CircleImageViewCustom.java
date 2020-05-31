package com.example.chekersgamepro.views.custom.circle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;
import com.example.chekersgamepro.views.custom.IBaseViewAnimationAngle;

import de.hdodenhof.circleimageview.CircleImageView;

public class CircleImageViewCustom extends CircleImageView implements IBaseViewAnimationAngle {

    private final float DENSITY = getResources().getDisplayMetrics().density;
    private float WIDTH = (float) 0.8;
    private final float BORDER_WIDTH_DEFAULT = WIDTH * DENSITY;

    private static final int START_ANGLE_POINT = 270;

    private final Paint paint;

    private float angle;

    private int margin;
    private int strokeWidth;

    private long durationAnimation = getResources().getInteger(R.integer.view_default_draw_circle_animation_duration);

    public CircleImageViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);

        margin = (int) DisplayUtil.Companion.convertDpToPixel(getResources().getDimensionPixelSize(R.dimen.view_draw_circle_margin));
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(BORDER_WIDTH_DEFAULT);
        //Circle color
        paint.setColor(Color.WHITE);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setDrawingCacheEnabled(true);
        //Initial Angle (optional, it can be zero)
        angle = 0;
    }

    @Override
    public boolean isAnimationViewAngle() {
        return true;
    }

    @Override
    public void setMarginDrawCircle(int dp){
        this.margin = (int) DisplayUtil.Companion.convertDpToPixel(dp);
    }

    @Override
    public void setCircleWidth(float width){
        paint.setStrokeWidth(width* DENSITY);
    }

    @Override
    public void setCircleColor(int color){
        this.paint.setColor(color);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();

        int centerX = clipBounds.centerX();
        int centerY = clipBounds.centerY();

        float left = centerX - centerY + margin;
        float top = clipBounds.top + margin;
        float right = centerY + centerX - margin;
        float bottom = clipBounds.bottom - margin;



        RectF rect = new RectF(left, top, right, bottom);

        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public long getDrawAnimationDuration() {
        return durationAnimation;
    }

    @Override
    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public void requestLayoutNow() {
        requestLayout();
    }

    @Override
    public void startAnimation(Animation animation) {
        ((AngleAnimation)animation).setAnimation(true);
        super.startAnimation(animation);
    }

    @Override
    public void setDurationAnimation(long durationAnimation) {
        this.durationAnimation = durationAnimation;
    }
}
