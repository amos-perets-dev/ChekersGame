package com.example.chekersgamepro.graphic.cell;

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
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Observable;

@SuppressLint("AppCompatCustomView")
public class CellView extends ImageView{

    private final int DRAWABLE_ID = R.drawable.cell_1;

    private Paint paint = new Paint();

    private boolean isMasterCell = false;
    private Bitmap queenIcon;

    public CellView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(10f);
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

    public CellView setBg(float alpha, boolean isMasterCell){

        setBackgroundResource(DRAWABLE_ID);
        setAlpha(alpha);
        queenIcon = drawableToBitmap(getContext().getDrawable(R.drawable.ic_crown));
        return this;
    }

    private int pixels = (int) DisplayUtil.Companion.convertDpToPixel(5);


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();
        RectF rectF = new RectF(clipBounds.left + pixels, clipBounds.top + pixels, clipBounds.right - pixels, clipBounds.bottom - pixels);
        if (isMasterCell) {
            canvas.drawBitmap(queenIcon, null, rectF, null);
            isMasterCell = false;
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

    public Observable<CellView> getCellClick(){
        return RxView.clicks(this)
                .switchMap(ignored -> Observable.just(this));
    }
    public CellView checked(int color, boolean drawQueen){
        if (color == DataGame.ColorCell.CLEAR_CHECKED){
            setBackgroundResource(DataGame.ColorCell.CLEAR_CHECKED);
        } else {
            setBackgroundColor(color);
            if (drawQueen){
                isMasterCell = true;
                invalidate();
            }
        }
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