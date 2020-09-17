package com.example.chekersgamepro.screens.game.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.db.repository.RepositoryManager;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ComputerIconView {

    private final GameBoardView gameBoardView;
    private final ImageView computerIcon;

    private boolean isOwner;

    private float translationY;
    private float translationX;

    private int gameMode;

    public ComputerIconView(Activity activity, GameBoardView gameBoardView, Observable<Boolean> isOwnerAsync) {
        this.gameBoardView = gameBoardView;
        this.computerIcon = activity.findViewById(R.id.computer_sign);
        isOwnerAsync.subscribe(this::setIsOwner);
    }

    private void setIsOwner(boolean isOwner){
        this.isOwner = isOwner;
    }

    Completable initComputerIcon(Integer gameMode, int timerHeight) {

        this.gameMode = gameMode;
        // set the icon computer location on the screen
        computerIcon.setRotation(isOwner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? 180 : 0);

        int translationYTop = gameBoardView.getTop() - computerIcon.getMeasuredHeight() - 10 - timerHeight;
        int translationYBottom = gameBoardView.getBottom() + 10 + timerHeight;

        translationY = isOwner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? translationYTop : translationYBottom;
        translationX = gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2);
        computerIcon.setTranslationY(translationY);
        computerIcon.setTranslationX(translationX);

        return Completable.complete();
    }

    private ViewPropertyAnimator viewPropertyAnimator;

    public Completable animateComputerIcon(boolean isReturnStartPointAfterClick, Point point, CellView cellView) {

        if (point == null || cellView == null)
            return Completable.error(new Throwable("point == null || cellView == null"));

        if (viewPropertyAnimator != null) viewPropertyAnimator.cancel();

        return Completable.create(emitter -> {
            viewPropertyAnimator = computerIcon
                    .animate()
                    .withLayer()
                    .translationY(isOwner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? point.y - (cellView.getMeasuredHeight() / 2) : point.y + (cellView.getMeasuredHeight() / 2))
                    .translationX(point.x)
                    .setDuration(300)
                    .withEndAction(() -> {
                        animateClick(isReturnStartPointAfterClick, emitter);
                        animateIconToStartPoint(isReturnStartPointAfterClick, emitter);
                    });
            viewPropertyAnimator
                    .start();
            emitter.setDisposable(Disposables.fromAction(() -> viewPropertyAnimator.cancel()));
        });

    }

    private void animateIconToStartPoint(boolean isReturnStartPointAfterClick, CompletableEmitter emitter) {
        if (!isReturnStartPointAfterClick) return;
        computerIcon
                .animate()
                .withLayer()
                .translationY(translationY)
                .translationX(translationX)
                .withEndAction(emitter::onComplete)
                .setDuration(250)
                .start();
    }

    private void animateClick(boolean isReturnToStartPointAfterClick, CompletableEmitter emitter) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(computerIcon,
                PropertyValuesHolder.ofFloat("scaleX", 1, 0.6f, 1),
                PropertyValuesHolder.ofFloat("scaleY", 1, 0.6f, 1))
                .setDuration(200);

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isReturnToStartPointAfterClick) emitter.onComplete();
            }
        });

        objectAnimator
                .start();
    }

    public Completable showWithAnimate() {

        return Completable.create(emitter -> computerIcon
                .animate()
                .withLayer()
                .alpha(1)
                .setDuration(300)
                .withEndAction(emitter::onComplete)
                .start());

    }
}
