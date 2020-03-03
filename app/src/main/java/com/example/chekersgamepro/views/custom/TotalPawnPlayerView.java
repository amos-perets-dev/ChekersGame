package com.example.chekersgamepro.views.custom;

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

public class TotalPawnPlayerView extends CardView {

    private Paint paintText = new Paint();

    private Bitmap iconPlayerQueen = null;
    private Bitmap iconPlayerRegular = null;

    private String textTotalQueen = "0";
    private String textTotalRegular = "12";

    public TotalPawnPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paintText.setColor(Color.BLACK);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setAntiAlias(true);
        paintText.setTextSize(16 * getResources().getDisplayMetrics().density);

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (textTotalQueen == null || textTotalRegular == null || iconPlayerRegular == null || iconPlayerQueen == null) return;

        Rect clipBounds = canvas.getClipBounds();


        //Calculate bounds
        int top = clipBounds.top + 25;
        int bottom = clipBounds.bottom - 25;
        int height = bottom - top;

        int left = clipBounds.left + 25;
        int right = clipBounds.right - 25;
        int width = right - left;

        float topText = (float) (height * 1.05);

        //Draw regular icon
        int rightRegularPawn = left + height;
        drawIcon(canvas, iconPlayerRegular, new RectF(left, top, rightRegularPawn, bottom));

        //Draw total regular pawns
        int rightRegularPawnText = rightRegularPawn + 15;
        drawTotalPawns(canvas, rightRegularPawnText, topText, textTotalRegular);

        //Draw queen icon
        float leftQueen = (width * 0.6f);
        float rightQueenPawn = leftQueen + height;
        drawIcon(canvas, iconPlayerQueen, new RectF(leftQueen, top, rightQueenPawn, bottom));

        float rightQueenPawnText = rightQueenPawn + 15;
        //Draw total queen pawns
        drawTotalPawns(canvas, rightQueenPawnText, topText, textTotalQueen);

    }

    private void drawIcon(Canvas canvas, Bitmap icon, RectF rectF){
        canvas.drawBitmap(icon, null, rectF, null);

    }

    private void drawTotalPawns(Canvas canvas, float x, float y, String totalPawns){
        Rect rectQueenPawn = new Rect();
        paintText.getTextBounds(totalPawns, 0, totalPawns.length(), rectQueenPawn);
        canvas.drawText(totalPawns, x, y, paintText);
    }

    public TotalPawnPlayerView setIconPlayerQueen(Drawable iconPlayerQueen) {
        this.iconPlayerQueen = drawableToBitmap(iconPlayerQueen);
        return this;
    }

    public TotalPawnPlayerView setIconPlayerRegular(Drawable iconPlayerRegular) {
        this.iconPlayerRegular = drawableToBitmap(iconPlayerRegular);
        return this;
    }



    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap1 = bitmapDrawable.getBitmap();
            if (bitmap1 != null) {

                return bitmap1;
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setTextTotalPawns(String textTotalRegular, String textTotalQueen) {
        this.textTotalRegular = textTotalRegular;
        this.textTotalQueen = textTotalQueen;
        invalidate();
    }
}
