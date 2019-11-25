package com.example.chekersgamepro;

import android.graphics.Point;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.game_board.GameInitialImpl;
import com.example.chekersgamepro.data.game_validation.GameValidationImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.data_game.DataGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.reactivex.Observable;

public class GameManager {

    private GameInitialImpl gameInitialImpl = new GameInitialImpl();

    private GameValidationImpl gameValidation;

    private GameCreatorImpl gameCreatorChecked;

    private boolean isPlayerOneTurn;

    private List<ChangePlayerListener> changePlayerListListeners = new ArrayList<>();

    private DataGame dataGame = DataGame.getInstance();

    public void initGame(int x, int y, int width, int height, int gameMode) {

        isPlayerOneTurn = new Random().nextBoolean();

        this.gameInitialImpl
                .setX(x)
                .setY(y)
                .setWidth(width)
                .setHeight(height)
                .setGameMode(gameMode);
        this.gameInitialImpl.initBorderLines();
        this.gameInitialImpl.initGameBoard();
        this.gameInitialImpl.initPawns();

        gameValidation = new GameValidationImpl();

        gameCreatorChecked = new GameCreatorImpl(gameValidation);

        dataGame.addChanePlayerListener(changePlayerListListeners);
    }

    public int getWidthCell(){
        return gameInitialImpl.getWidthCell();
    }

    public List<BorderLine> getBorderLines() {
        return gameInitialImpl.getBorderLines();
    }


    public int getHeightCell(){
        return gameInitialImpl.getHeightCell();
    }

    public int getGameBoardSize() {
        return gameInitialImpl.getGameBoardSize();
    }

    public int getColorBorderCell() {
        return gameInitialImpl.getColorBorderCell();
    }

    public int getBorderWidth() {
        return gameInitialImpl.getBorderWidth();
    }

    public Map<Point, PawnDataImpl> getPawns() {
        return gameInitialImpl.getPawns();
    }

    public Map<Point, CellDataImpl> getCells() {
        return gameInitialImpl.getCells();
    }

    public List<DataCellViewClick> createRelevantCellsStart() {
        return gameCreatorChecked.createRelevantCellsStart();
    }

    private String getPlayerName() {
        return   (isPlayerOneTurn ? "PLAYER 1" : dataGame.getGameMode() == DataGame.COMPUTER_GAME_MODE ? "COMPUTER" : "PLAYER 2");
    }

    public String nextTurnChangePlayer() {
        isPlayerOneTurn = !isPlayerOneTurn;
        for (ChangePlayerListener changePlayerListener : changePlayerListListeners){
            changePlayerListener.onChangePlayer(isPlayerOneTurn);
        }
        return getPlayerName();
    }

    public List<DataCellViewClick> createOptionalPathByCell(float x, float y) {
        return gameCreatorChecked.createOptionalPath(x, y);
    }

    public List<Point> getMovePawnPath(float x, float y) {
        return gameCreatorChecked.getMovePawnPath(x, y);
    }

    public Set<Point> getOptionalPointsListComputer(){
        return gameCreatorChecked.getOptionalPointsListComputer();
    }

    public PawnDataImpl removePawnIfNeeded() {
        return gameCreatorChecked.removePawnIfNeeded();
    }

    public void clearData() {
        gameCreatorChecked.clearData();
    }

    public void updatePawnKilled() {
        gameCreatorChecked.updatePawnKilled();
    }

    public void actionAfterPublishMovePawnPath() {
        gameCreatorChecked.actionAfterPublishMovePawnPath();
    }

    public Point getPointPawnByCell(Point pointByCell) {
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(dataGame.getCellByPoint(pointByCell).getPointStartPawn());
        return pawnByPoint != null ? pawnByPoint.getStartXY() : null;
    }

    public boolean isQueenPawn(Point currPawnPoint) {
        return gameValidation.isQueenPawn(currPawnPoint);
    }

    public boolean isSomePlayerWin() {
        return gameValidation.isSomePlayerWin();
    }

    public String getWinPlayerName() {
        return "The winning player is " + (isPlayerOneTurn ? "PLAYER TWO" : "PLAYER ONE" );
    }

    public boolean isPlayerOneTurn() {
        return isPlayerOneTurn;
    }

    public boolean isComputerModeGame() {
        return dataGame.getGameMode() == DataGame.COMPUTER_GAME_MODE;
    }

    public interface ChangePlayerListener{
        void onChangePlayer(boolean isPlayerOneTurn);
    }

}
