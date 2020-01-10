package com.example.chekersgamepro.screens.game;

import android.graphics.Point;
import android.widget.ImageView;

import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;

public class ComputerIconView {

    private GameViewsManager gameViewsManager;

    public ComputerIconView(GameViewsManager gameViewsManager) {
        this.gameViewsManager = gameViewsManager;
    }

    public void animateComputerIcon(boolean isReturnToStartPoint, Point point){
        GameBoardView gameBoardView = gameViewsManager
                .getGameBoardView();
        ImageView computerIcon = gameViewsManager.getComputerIcon();

        CellView cellView = gameViewsManager.getCellViewByPoint(point);
        computerIcon
                .animate()
                .translationY(point.y + (cellView.getMeasuredHeight() / 2))
                .translationX(point.x)
                .setDuration(300)
                .withEndAction(() -> {
                    cellView.performClick();

                    if (isReturnToStartPoint){
                        computerIcon
                                .animate()
                                .translationY(gameBoardView.getBottom() + 10)
                                .translationX(gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2))
                                .setDuration(250)
                                .start();
                    }
                })
                .start();
    }

}
