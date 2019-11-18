package com.example.chekersgamepro.graphic.pawn;

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
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("AppCompatCustomView")
public class PawnView extends ImageView{

    private Paint paint = new Paint();

    private Bitmap bitmap;

    private float indexRemovePawn = 1;
    private float indexTranslatePawn = 1;

    private Disposable disposableRemovePawn;

    public PawnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2.5f);

    }

    public PawnView setClickablePawn(boolean isClickable){
        setClickable(isClickable);
        return this;
    }

    public PawnView setEnabledPawn(boolean isClickable){
        setEnabled(isClickable);
        return this;
    }

    public PawnView setXY(Integer x, Integer y){
        if (x != null) setX(x);
        if (y != null) setY(y);
        return this;
    }

    public PawnView setIcon(int drawable){
        bitmap = drawableToBitmap(ContextCompat.getDrawable(getContext(), drawable));
        return this;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();

//        clear(canvas);

        //draw shadow
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle( getPivotX() + 6, getPivotY() + 6,  getPivotY() - 8, paint );

        //draw bitmap
        RectF rectF = new RectF(clipBounds.left + 10, clipBounds.top  + 10, clipBounds.right  - 10, clipBounds.bottom - 10);
        if (bitmap != null){
            canvas.drawBitmap(bitmap, null, rectF, null );
        }
    }

    /**
     * Clear the canvas
     * @param canvas
     */
    private void clear (Canvas canvas) {
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

    public Observable<PawnView> getPawn(){
        return RxView.touches(this)
                .filter(motionEvent -> motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                .switchMap(ignored -> Observable.just(this));
    }

    public Observable<PawnView> getPawnLocationLayoutChange(){
        return RxView.layoutChanges(this)
                .distinctUntilChanged()
                .switchMap(ignored -> Observable.just(this));
    }


    public Observable<PawnView> getPawnLocationDrags(){
        return RxView.drags(this)
                .distinctUntilChanged()
                .switchMap(ignored -> Observable.just(this));
    }

    public Observable<PawnView> getPawnLocationGlobalLayouts(){
        return RxView.globalLayouts(this)
                .switchMap(ignored -> Observable.just(this));
    }

    public Completable translateView(int x, int y){

        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {

                for (indexTranslatePawn = 0.01f; (int)getY() != y || (int)getX() != x; indexTranslatePawn++){
                    if ((int)getY() != y){
                        setTranslationY(indexTranslatePawn);
                    }

                    if ((int)getX() != x){
                        setTranslationX(indexTranslatePawn);
                    }

                }

                emitter.onComplete();
            }
        });
    }

//    public Completable translateView(int x, int y){
//        return Completable.create(new CompletableOnSubscribe() {
//            @Override
//            public void subscribe(CompletableEmitter emitter) throws Exception {
//
//                for (indexRemovePawn = 1; (int)getY() != y || (int)getX() != x; indexRemovePawn++){
//                    if ((int)getY() != y){
//                        setTranslationY(indexRemovePawn);
//                    }
//
//                    if ((int)getX() != x){
//                        setTranslationX(indexRemovePawn);
//                    }
//                }
//                emitter.onComplete();
//            }
//        });
//    }

    public void removePawn(){

        indexRemovePawn = 1;

        if (disposableRemovePawn != null){
            disposableRemovePawn.dispose();
        }

        disposableRemovePawn = Observable.interval(10, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(input -> indexRemovePawn = indexRemovePawn - 0.2f)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (indexRemovePawn > 0) {
                            setAlpha(indexRemovePawn);
                            invalidate();
                        } else {
                            setVisibility(GONE);
                        }
                    }
                });


//                animate()
//                .withLayer()
//                /* .translationY(y)
//                 .translationX(x)*/
//                .alpha(0)
//                .setDuration(400)
//                .withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        // set the pawn view
//                        setVisibility(GONE);
////                        setClickable(false);
////                        setEnabled(false);
//
//                        x += 120;
//
//                        if (x >= 600) {
//                            y = 120;
//                        }
//                    }
//                })
//                .withStartAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        isAnimationProcess = true;
//                        prevPawnView = PawnView.this;
//                    }
//                })
//                .start();
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