package com.example.chekersgamepro.views.custom;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class PlayerNameView extends CardView {

    private Paint paintText = new Paint();
    private Paint paintRect = new Paint();

    private String text;

//    private Bitmap iconPlayer = null;

    private boolean isTurn;

    public PlayerNameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paintText.setColor(Color.BLACK);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setAntiAlias(true);
        paintText.setTextSize(16 * getResources().getDisplayMetrics().density);
        paintText.setShadowLayer(10, 1, 1, Color.GRAY);

        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setAntiAlias(true);
        paintRect.setStrokeWidth(3f);
    }

    public void setText(String text) {
        this.text = text;
    }

//    public void setIcon(Drawable icon) {
//        iconPlayer = drawableToBitmap(icon);
//    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (text == null /*|| iconPlayer == null*/) return;

        //Draw player name
        Rect rect = new Rect();
        paintText.getTextBounds(text, 0, text.length(), rect);

        int x = ((getMeasuredWidth() - rect.width()) / 2);
        int y = (int) (getMeasuredHeight() - (rect.height()  * 1.175));

        canvas.drawText(text, x, y, paintText);

        Rect clipBounds = canvas.getClipBounds();

//        //Draw bitmap, icon
//        int top = clipBounds.top + 25;
//        int bottom = clipBounds.bottom - 25;
//        int sumTop = bottom - top;
//        int left = clipBounds.left + 25;
//        int right = clipBounds.right - 25;
//
//        RectF rectF;
//
//        rectF = new RectF(left, top, left + sumTop, bottom);
//
//        canvas.drawBitmap(iconPlayer, null, rectF, null);

        // Draw rectangle
        Rect rectView = new Rect(clipBounds.left + 5, clipBounds.top + 5, clipBounds.right - 5, clipBounds.bottom - 5);

        if (isTurn){
            paintRect.setColor(Color.GREEN);
        } else {
            paintRect.setColor(Color.TRANSPARENT);
        }
        canvas.drawRect(rectView, paintRect);

    }

//    public Bitmap drawableToBitmap(Drawable drawable) {
//        Bitmap bitmap;
//
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            Bitmap bitmap1 = bitmapDrawable.getBitmap();
//            if (bitmap1 != null) {
//
//                return bitmap1;
//            }
//        }
//
//        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
//            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
//        } else {
//            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }

    public void setIsTurn(boolean isTurn) {
        this.isTurn = isTurn;
        invalidate();

        if (isTurn){
            ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat("scaleX", 1, 1.15f, 1),
                    PropertyValuesHolder.ofFloat("scaleY", 1, 1.15f, 1))
                    .setDuration(400)
                    .start();
        }
    }
}
