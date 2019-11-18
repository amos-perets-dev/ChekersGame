package com.example.chekersgamepro.graphic.cell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

@SuppressLint("AppCompatCustomView")
public class CellView extends ImageView{

    private final int COLOR_CAN_START = Color.YELLOW;

    private final int COLOR_CAN_KIELLD = Color.BLUE;

    private final int COLOR_CHECKED = Color.GREEN;

    private final int DRAWABLE_ID = R.drawable.cell_1;

    private Paint paint = new Paint();

    private Bitmap bitmap = null;

    private int color;

    private boolean drawDefault = true;

    public CellView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(10f);

    }

    public CellView setClickableCell(boolean isClickable){
        setClickable(isClickable);
        return this;
    }

    public CellView setEnabledCell(boolean isClickable){
        setEnabled(isClickable);
        return this;
    }

    public CellView invalidateSelf() {
        invalidate();
        return this;
    }


    public int getColorCellCanStart(){
        return COLOR_CAN_START;
    }

    public int getColorCheckedByUser(){
        return COLOR_CHECKED;
    }

    public CellView setColor(int color) {
        paint.setColor(color);
        return this;
    }

    public CellView setXY(Integer x, Integer y){
//        Log.d("TEST_GAME", "BEFORE: X: " + getX() +", Y: " + getY());
        if (x != null) setX(x);
        if (y != null) setY(y);
//        Log.d("TEST_GAME", "AFTER: X: " + getX() +", Y: " + getY());
        return this;
    }

    public CellView setBg(float alpha){

//        setBackgroundResource(DRAWABLE_ID);
        bitmap = drawableToBitmap(ContextCompat.getDrawable(getContext(), DRAWABLE_ID));
        setAlpha(alpha);

        return this;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
         Rect clipBounds = canvas.getClipBounds();

//        canvas.drawRect(canvas.getClipBounds(), paint);


        if (drawDefault){
            //draw bitmap
            RectF rectF = new RectF(clipBounds);
            if (bitmap != null){
                canvas.drawBitmap(bitmap, null, rectF, null );
            }
        } else {
            paint.setColor(color);
            canvas.drawRect(clipBounds, paint);
        }
    }

    public CellView setWidth(int widthCell) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = widthCell;

        setLayoutParams(layoutParams);
        return this;
    }

    public CellView setHeight(int heightCell) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = heightCell;

        setLayoutParams(layoutParams);
        return this;
    }


    public Observable<CellView> getCell(){
        Log.d("TEST_GAME", " public Observable<CellView> getCell(){");
        return RxView.touches(this)
                .filter(motionEvent -> motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                .switchMap(ignored -> Observable.just(this));
    }

    public CellView clearChecked(boolean isValid, boolean isParent){

        if (isParent && isValid){
            color = Color.YELLOW;
            drawDefault = false;
        } else {
            drawDefault = true;
        }
        invalidate();
        return this;
    }

    public CellView clearChecked(){
        drawDefault = true;
        invalidate();

        return this;
    }

    public CellView checked(int color){
        this.color = color;
        drawDefault = false;
        invalidate();

        return this;
    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap1 = bitmapDrawable.getBitmap();
            if(bitmap1 != null) {

                return bitmap1;
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}