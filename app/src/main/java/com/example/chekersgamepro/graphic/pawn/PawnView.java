package com.example.chekersgamepro.graphic.pawn;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@SuppressLint("AppCompatCustomView")
public class PawnView extends pl.droidsonroids.gif.GifImageView {

    private Paint paint = new Paint();

    private int specialIconRes;
    private Bitmap queenIcon = null;
    private int regularIconRes;

    private boolean isAlreadyChangeIcon = false;

    private boolean isDrawQueenPawn = false;

    public PawnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(2.5f);
        setScaleX(0.92F);
        setScaleY(0.92F);

    }

    public PawnView setXY(Integer x, Integer y) {
        if (x != null) setX(x);
        if (y != null) setY(y);
        return this;
    }

    public PawnView setRegularIcon(int drawable) {
        regularIconRes = drawable;
        setBackgroundResource(drawable);
//        icon = regularIcon = drawableToBitmap(ContextCompat.getDrawable(getContext(), drawable));
        return this;
    }

    public PawnView setQueenIcon(int drawable) {
        specialIconRes = drawable;

        return this;
    }

    private int pixels = (int) DisplayUtil.Companion.convertDpToPixel(10);
    private int pixelsTop = (int) DisplayUtil.Companion.convertDpToPixel(6);

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();

        Log.d("TEST_GAME", "PawnView -> 1 onDraw");
//        if (!isAlreadyChangeIcon) {
        Log.d("TEST_GAME", "PawnView -> 2 onDraw");

        RectF rectF = new RectF(clipBounds.left + pixels, clipBounds.top + pixelsTop, clipBounds.right - pixels, clipBounds.bottom - pixels);

        if (queenIcon != null) {
            Log.d("TEST_GAME", "PawnView -> 3 onDraw");

            canvas.drawBitmap(queenIcon, null, rectF, paint);
        }
//        }

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
                .setDuration(600)
                .withStartAction(this::startFire)
                .withEndAction(() -> {
                    setScaleX(1F);
                    setScaleY(1F);
                    setVisibility(GONE);
                })
                .start();
    }

    private void startFire() {
        animate()
                .withLayer()
                .setDuration(200)
                .withEndAction(() -> {
                    setBackgroundResource(R.drawable.fire_1);
                    setScaleX(1.6F);
                    setScaleY(1.6F);
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
        Log.d("TEST_GAME", "PawnView -> 1 setIcon");

        if (isSpecialIcon && !isAlreadyChangeIcon) {
            Log.d("TEST_GAME", "PawnView -> 2 setIcon");

            ObjectAnimator animatePulse = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat("scaleX", 0.2f, 1f, 0.92f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.2f, 1f, 0.92f));
            animatePulse.setDuration(1000);

            animatePulse.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAlreadyChangeIcon = true;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatePulse.start();
        }

        if (isSpecialIcon) {
            queenIcon = drawableToBitmap(ContextCompat.getDrawable(getContext(), R.drawable.ic_crown));
            invalidate();
        }
    }

    private PublishSubject<Boolean> isStartIterateMovePawn = PublishSubject.create();

    public Observable<Boolean> isStartIterateMovePawn() {
        return isStartIterateMovePawn.hide()
                .startWith(true);
    }

    public void animatePawnMove(List<Point> pointsListAnimatePawnMove, int indexPointsListAnimatePawn) {

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

}