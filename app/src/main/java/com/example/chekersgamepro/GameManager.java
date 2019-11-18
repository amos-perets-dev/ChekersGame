package com.example.chekersgamepro;

import android.graphics.Point;
import android.util.Pair;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.game_board.GameInitialImpl;
import com.example.chekersgamepro.data.game_validation.GameValidationImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameManager {

    private GameInitialImpl gameInitialImpl = new GameInitialImpl();

    private GameValidationImpl gameValidation;

    private GameCreatorImpl gameCreatorChecked;

    private boolean isPlayerOneTurn;

    private List<ChangePlayerListener> changePlayerListListeners = new ArrayList<>();

    public void initGame(int x, int y, int width, int height) {

        isPlayerOneTurn = new Random().nextBoolean();

        this.gameInitialImpl
                .setX(x)
                .setY(y)
                .setWidth(width)
                .setHeight(height);
        this.gameInitialImpl.initBorderLines();
        this.gameInitialImpl.initGameBoard();
        this.gameInitialImpl.initPawns();

        gameValidation = new GameValidationImpl(isPlayerOneTurn, changePlayerListListeners);

        gameCreatorChecked = new GameCreatorImpl(isPlayerOneTurn, gameValidation, changePlayerListListeners);
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

    public List<Point> createRelevantCellsStart() {
        return gameCreatorChecked.createRelevantCellsStart();

    }

    private String getPlayerName() {
        return "PLAYER " + (isPlayerOneTurn ? "1" : "2");
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

    public Pair<Point,  List<Point>> getMovePawnPath(float x, float y) {
        return gameCreatorChecked.getMovePawnPath(x, y);
    }

    public PawnDataImpl removePawnIfNeeded() {
        return gameCreatorChecked.removePawnIfNeeded();
    }

    public void clearData() {
        gameCreatorChecked.clearData();
    }

    public boolean isInOptionalPath(float x, float y) {
        return gameCreatorChecked.isInOptionalPath(x, y);
    }

    public boolean isInOptionalPathValid(float x, float y) {
        return gameCreatorChecked.isInOptionalPathValidCell(x, y);
    }

    public void setCurrentTurnData() {

        gameCreatorChecked.setCurrentTurnData();

    }

    public interface ChangePlayerListener{
        void onChangePlayer(boolean isPlayerOneTurn);
    }

}
