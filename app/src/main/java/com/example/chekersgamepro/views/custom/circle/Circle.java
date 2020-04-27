package com.example.chekersgamepro.views.custom.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class Circle extends View {

    private static final int START_ANGLE_POINT = 270;

    private final Paint paint;
    private RectF rect;

    private float angle;
    final int strokeWidth;

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);

        strokeWidth = 40;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.RED);

        //size 200x200 example
        rect = new RectF(strokeWidth, strokeWidth, 200 + strokeWidth, 200 + strokeWidth);

        //Initial Angle (optional, it can be zero)
        angle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setRect(@NotNull View itemView) {
        //size 200x200 example
//        Rect clipBounds = itemView.getClipBounds();
//        rect = new RectF(clipBounds.left, clipBounds.top, clipBounds.right, clipBounds.bottom);
    }
}