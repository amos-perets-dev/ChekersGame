package com.example.chekersgamepro.graphic.pawn;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@SuppressLint("AppCompatCustomView")
public class PawnView extends ImageView {

    private Paint paint = new Paint();

    private Bitmap regularIcon;

    private Bitmap specialIcon;

    private Bitmap icon;

    private boolean isAlreadyChangeIcon = false;

    private boolean isCanBeDraw = false;

    public PawnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(2.5f);

    }

    public PawnView setEnabledPawn(boolean isClickable) {
        setEnabled(isClickable);
        return this;
    }

    public PawnView setXY(Integer x, Integer y) {
        if (x != null) setX(x);
        if (y != null) setY(y);
        return this;
    }

    public PawnView setRegularIcon(int drawable){
        icon = regularIcon = drawableToBitmap(ContextCompat.getDrawable(getContext(), drawable));
        return this;
    }

    public PawnView setQueenIcon(int drawable) {
        specialIcon = drawableToBitmap(ContextCompat.getDrawable(getContext(), drawable));
        return this;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();

        if (isCanBeDraw) {
            //draw shadow
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.DKGRAY);
            canvas.drawCircle(getPivotX() + 6, getPivotY() + 6, getPivotY() - 9, paint);

            //draw bitmap
            RectF rectF = new RectF(clipBounds.left + 10, clipBounds.top + 10, clipBounds.right - 10, clipBounds.bottom - 10);

            if (icon != null) {
                canvas.drawBitmap(icon, null, rectF, paint);
//                setRadius(90);
            }
        }

    }

    /**
     * Clear the canvas
     *
     * @param canvas
     */
    private void clear(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }

    public PawnView setWidth(int widthCell) {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = widthCell;

        setLayoutParams(layoutParams);
        return this;
    }

    public PawnView setHeight(int heightCell) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = heightCell;

        setLayoutParams(layoutParams);
        return this;
    }

    public Observable<PawnView> getPawnClick() {
        return RxView.clicks(this)
//                .filter(motionEvent -> motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                .switchMap(ignored -> Observable.just(this));
    }

    public void removePawn() {
        animate()
                .withLayer()
                .alpha(0)
                .setDuration(400)
                .withEndAction(() -> {
                    // set the visibility pawn
                    setVisibility(GONE);
                })
                .start();
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

    public void setIcon(boolean isSpecialIcon) {
        icon = isSpecialIcon ? specialIcon : regularIcon;

        if (isSpecialIcon && !isAlreadyChangeIcon) {
            isAlreadyChangeIcon = true;
            ObjectAnimator animatePulse = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));
            animatePulse.setDuration(300);

            animatePulse.setRepeatCount(3);
            animatePulse.setRepeatMode(ObjectAnimator.REVERSE);

            animatePulse.start();
        }
    }

    private PublishSubject<Boolean> isStartIterateMovePawn = PublishSubject.create();

    public Observable<Boolean> isStartIterateMovePawn() {
        return isStartIterateMovePawn.hide()
                .startWith(true);
    }

    public void animatePawnMove(List<Point> pointsListAnimatePawnMove, int indexPointsListAnimatePawn){

        Point currPoint = pointsListAnimatePawnMove.get(indexPointsListAnimatePawn);
        final int index = ++indexPointsListAnimatePawn;
        this
                .animate()
                .withLayer()
                .translationY(currPoint.y)
                .translationX(currPoint.x)
                .setDuration(250)
                .withStartAction(() -> {
                    this.setElevation(10f);
                    isStartIterateMovePawn.onNext(true);
                })
                .withEndAction(() -> {

                    this.setElevation(0);
                    if (index < pointsListAnimatePawnMove.size()) {
                        animatePawnMove(pointsListAnimatePawnMove, index);
                    } else {
                        isStartIterateMovePawn.onNext(false);
                    }
                })
                .start();
    }

    public PawnView setIsReady(boolean isReady) {
        this.isCanBeDraw = isReady;
        invalidate();
        return this;
    }
}