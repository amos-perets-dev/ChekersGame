package com.example.chekersgamepro.views.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.checkers.CheckersImageUtil;

public class ImageViewShadow extends AppCompatImageView {
    private Paint mShadow = new Paint();

    private Bitmap bitmap;

    private int shadowColor = Color.DKGRAY;
    private int iconColor = Color.WHITE;

    public ImageViewShadow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    public void setBackground(Drawable drawable) {
        this.bitmap = CheckersImageUtil.create().drawableToBitmap(drawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null){
            drawBitmap(canvas, bitmap);

        }
    }

    private void drawBitmap(Canvas canvas, Bitmap sourceBitmap) {

        Bitmap resultBitmap =
                Bitmap.createScaledBitmap(
                        sourceBitmap
                        , (int) (sourceBitmap.getWidth() * 1.08)
                        , (int) (sourceBitmap.getHeight() * 1.08)
                        , false);

        canvas.drawBitmap(resultBitmap, 1.5f, 1.5f, getPaint(this.shadowColor));
        canvas.drawBitmap(resultBitmap, 0f, 0f, getPaint(this.iconColor));
    }

    public void setShadowColor(int color){
        this.shadowColor = color;
    }

    public void setIconColor(int color){
        this.iconColor = color;
        invalidate();
    }

    private Paint getPaint(int color){
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        paint.setColorFilter(filter);
        return paint;
    }
}
