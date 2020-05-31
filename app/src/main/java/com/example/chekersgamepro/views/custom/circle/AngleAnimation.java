package com.example.chekersgamepro.views.custom.circle;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.example.chekersgamepro.views.custom.IBaseViewAnimationAngle;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;

public class AngleAnimation extends Animation {

    private IBaseViewAnimationAngle view;

    private float oldAngle;
    private float newAngle;

    private boolean isAnimation;

    public AngleAnimation(IBaseViewAnimationAngle view) {
        this.oldAngle = view.getAngle();
        this.view = view;
        setDuration(view.getDrawAnimationDuration());
        setAnimation(true);
    }

    public Observable<Boolean> isAnimationCircleFinish() {
        return Observable.create(emitter -> {
            AnimationListener listener = new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    emitter.onNext(false);
//                    Log.d("TEST_GAME", "AngleAnimation -> onAnimationStart");

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (isAnimation){
                        emitter.onNext(true);
                    }
//                    Log.d("TEST_GAME", "AngleAnimation -> onAnimationEnd");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            setAnimationListener(listener);
            emitter.setDisposable(Disposables.fromAction(() -> setAnimationListener(null)));
        });
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        if (!isAnimation) return;
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        view.setAngle(angle);
        view.requestLayoutNow();
    }


    public void setAngle(boolean isSelected) {
        this.newAngle = isSelected ? 360 : 0;
    }

    public void clearAnimate() {
        cancelAnimate();
        view.setAngle(0);
        view.requestLayoutNow();
    }


    public void cancelAnimate() {
        cancel();
        setAnimation(false);
    }

    public void setAnimation(boolean animation) {
        this.isAnimation = animation;
    }
}