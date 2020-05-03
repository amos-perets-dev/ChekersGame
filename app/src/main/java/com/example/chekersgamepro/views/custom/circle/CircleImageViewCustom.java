package com.example.chekersgamepro.views.custom.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class CircleImageViewCustom extends CircleImageView {

    private static final int START_ANGLE_POINT = 270;

    private final Paint paint;

    private float angle;

    private final int margin;

    public CircleImageViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);

        int strokeWidth = 4;
        margin = (int) DisplayUtil.Companion.convertDpToPixel(15);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.WHITE);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setDrawingCacheEnabled(true);
        //Initial Angle (optional, it can be zero)
        angle = 0;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();
        //size 200x200 example
        RectF rect = new RectF(
                clipBounds.left + margin
                , clipBounds.top + margin
                , clipBounds.right - margin
                , clipBounds.bottom - margin);

        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public long getDrawAnimationDuration() {
        return getResources().getInteger(R.integer.activity_home_page_default_avatar_draw_circle_animation_duration);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
