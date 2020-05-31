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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.example.chekersgamepro.R;

public class TotalGamesTextView extends AppCompatTextView {

    private Paint paintText = new Paint();

    private Bitmap iconUp = null;
    private Bitmap iconDown = null;

    private String textTotalLoss = "0";
    private String textTotalWin = "0";

    public TotalGamesTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paintText.setColor(Color.WHITE);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setAntiAlias(true);
        paintText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.english_first));

        iconUp = drawableToBitmap(getResources().getDrawable(R.drawable.ic_arrow_up_icon));
        iconDown = drawableToBitmap(getResources().getDrawable(R.drawable.ic_arrow_down_icon));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (textTotalLoss == null || textTotalWin == null || iconDown == null || iconUp == null) return;

        Rect clipBounds = canvas.getClipBounds();


        //Calculate bounds
        int heightClipBounds = clipBounds.height();
        int marginIcon = (heightClipBounds / 5);
        int top = clipBounds.top + marginIcon;
        int bottom = clipBounds.bottom - marginIcon;
        int height = bottom - top;

        int left = clipBounds.left + marginIcon * 2;
        int right = clipBounds.right - marginIcon * 2;
        int width = right - left;

        //Draw regular icon
        int rightIconDown = left + height;
        drawIcon(canvas, iconDown, new RectF(left , top, rightIconDown, bottom));

        int marginTextLeft = (int) (clipBounds.width() * 0.05);

        //Draw total regular pawns
        int rightTextTotalWin = rightIconDown + marginTextLeft;
        drawTotalGames(canvas, rightTextTotalWin, textTotalWin);

        //Draw queen icon
        float leftTextTotalWin = (width * 0.7f);
        float rightIconUp = leftTextTotalWin + height;
        drawIcon(canvas, iconUp, new RectF(leftTextTotalWin, top, rightIconUp, bottom));

        float rightTextTotalLoss = rightIconUp + marginTextLeft;
        //Draw total queen pawns
        drawTotalGames(canvas, rightTextTotalLoss, textTotalLoss);

    }

    private void drawIcon(Canvas canvas, Bitmap icon, RectF rectF){
        canvas.drawBitmap(icon, null, rectF, null);

    }

    private void drawTotalGames(Canvas canvas, float x, String totalPawns){
        Rect rectQueenPawn = new Rect();
        paintText.getTextBounds(totalPawns, 0, totalPawns.length(), rectQueenPawn);

        Rect bounds = new Rect();
        paintText.getTextBounds(textTotalLoss, 0, textTotalLoss.length(), bounds);
        paintText.setTextSize((float) (getMeasuredHeight() * 0.65));

        int y = (int) ((canvas.getHeight() / 2) - ((paintText.descent() + paintText.ascent()) / 2)) ;

        canvas.drawText(totalPawns, x, y, paintText);
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

    public void setTextTotalGames(String textTotalWin, String textTotalLoss) {
        this.textTotalWin = textTotalWin;
        this.textTotalLoss = textTotalLoss;
        invalidate();
    }
}
