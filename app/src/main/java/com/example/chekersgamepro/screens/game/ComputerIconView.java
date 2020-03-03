package com.example.chekersgamepro.screens.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Point;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.db.repository.RepositoryManager;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.disposables.Disposables;

public class ComputerIconView {

    private final GameBoardView gameBoardView;
    private final ImageView computerIcon;

    private boolean owner = RepositoryManager.create().getPlayer().isOwner();

    private int translationYTop;
    private int translationYBottom;
    private float translationX;

    private int gameMode;

    public ComputerIconView(Activity activity, GameBoardView gameBoardView) {
        this.gameBoardView = gameBoardView;
        this.computerIcon = activity.findViewById(R.id.computer_sign);

    }

    public void initComputerIcon(Integer gameMode, ProgressBar progressBarTop) {
        this.gameMode = gameMode;
        if (gameMode == DataGame.Mode.COMPUTER_GAME_MODE || gameMode == DataGame.Mode.ONLINE_GAME_MODE) {

            computerIcon
                    .animate()
                    .withStartAction(() -> {
                        // set the icon computer location on the screen
                        computerIcon.setRotation(owner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? 180 : 0);

                        translationX = gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2);

                        translationYTop = gameBoardView.getTop() - computerIcon.getMeasuredHeight() - 10 - progressBarTop.getMeasuredHeight();
                        translationYBottom = gameBoardView.getBottom() + 10 + progressBarTop.getMeasuredHeight();

                        computerIcon.setTranslationY(owner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? translationYTop : translationYBottom);
                        computerIcon.setTranslationX(translationX);

                    })
                    .alpha(1)
                    .setDuration(500)
                    .start();
        }
    }
    private ViewPropertyAnimator viewPropertyAnimator;

    public Completable animateComputerIcon(boolean isReturnToStartPoint, Point point, CellView cellView) {

        if (point == null || cellView == null) return Completable.error(new Throwable("point == null || cellView == null"));

        if (viewPropertyAnimator != null) viewPropertyAnimator.cancel();
        return Completable.create(emitter ->
        {
            viewPropertyAnimator = computerIcon
                    .animate()
                    .translationY(owner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? point.y - (cellView.getMeasuredHeight() / 2) : point.y + (cellView.getMeasuredHeight() / 2))
                    .translationX(point.x)
                    .setDuration(300)
                    .withEndAction(() -> {
                        animateClick(emitter);

                        if (isReturnToStartPoint) animateIconToStartPoint();
                    });
            viewPropertyAnimator
                    .start();
            emitter.setDisposable(Disposables.fromAction(() -> viewPropertyAnimator.cancel()));
        });

    }

    private void animateIconToStartPoint() {
        computerIcon
                .animate()
                .translationY(owner || gameMode == DataGame.Mode.COMPUTER_GAME_MODE ? translationYTop : translationYBottom)
                .translationX(translationX)
                .setDuration(250)
                .start();
    }

    private void animateClick(CompletableEmitter emitter) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(computerIcon,
                PropertyValuesHolder.ofFloat("scaleX", 1, 0.7f, 1),
                PropertyValuesHolder.ofFloat("scaleY", 1, 0.7f, 1))
                .setDuration(220);

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                cellView.performClick();
                emitter.onComplete();
            }
        });

        objectAnimator
                .start();
    }

}
