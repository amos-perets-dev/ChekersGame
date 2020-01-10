package com.example.chekersgamepro.screens.game;

import android.graphics.Point;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.move.Move;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

public class CheckersViewModel extends ViewModel {


    private MutableLiveData<List<DataCellViewClick>> relevantCells = new MutableLiveData<>();

    private MutableLiveData<String> playerName = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> optionalPath = new MutableLiveData<>();

    private MutableLiveData<List<Point>> movePawn = new MutableLiveData<>();

    private MutableLiveData<Point> removePawn = new MutableLiveData<>();

    private MutableLiveData<String> winPlayer = new MutableLiveData<>();

    private MutableLiveData<Move> computerTurn = new MutableLiveData<>();

    private MutableLiveData<Boolean> nextTurn = new MutableLiveData<>();

    private MutableLiveData<Boolean> isNeedBlock = new MutableLiveData<>();

    private GameManager gameManager;

    public CheckersViewModel() {

        this.gameManager = new GameManager();

    }

    public Observable<Boolean> isNeedBlock(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, isNeedBlock));
    }

    public Observable<Move> getComputerStartTurn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerTurn));
    }

    public Observable<Boolean> getNextTurn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, nextTurn));
    }

    public Observable<String> getWinPlayerName(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, winPlayer));
    }

    public Observable<List<Point>> getMovePawn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, movePawn));
    }

    public Observable<List<DataCellViewClick>> getRelevantCells(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, relevantCells));
    }

    public Observable<String> getPlayerName(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, playerName));
    }

    public Observable<List<DataCellViewClick>> getOptionalPath(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, optionalPath));
    }

    public Observable<Point> removePawn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, removePawn));
    }

    public void initGame(int x, int y, int width, int height, int gameMode) {
        gameManager.initGame(x, y, width, height, gameMode);
    }

    public Map<Point, PawnDataImpl> getPawns() {
        return gameManager.getPawns();
    }

    public List<BorderLine> getBorderLines() {
        return gameManager.getBorderLines();
    }

    public Map<Point, CellDataImpl> getCells() {
        return gameManager.getCells();
    }

    public int getColorBorderCell() {
        return gameManager.getColorBorderCell();
    }

    public int getBorderWidth() {
        return gameManager.getBorderWidth();
    }

    public void nextTurn() {
        gameManager.clearData();
        playerName.postValue(gameManager.nextTurnChangePlayer());
        relevantCells.postValue(gameManager.createRelevantCellsStart());
        // if it is the computer turn need to block
        isNeedBlock.postValue(!isNeedBlock());

        if (gameManager.isSomePlayerWin()){
            winPlayer.postValue(gameManager.getWinPlayerName());
        }

        if (gameManager.isComputerModeGame() && !gameManager.isPlayerOneTurn()){
            Move moveAI = gameManager.getMoveAI();
            if (moveAI != null){
                computerTurn.postValue(moveAI);
            }
        }

    }

    public boolean isNeedBlock(){
        if (!gameManager.isComputerModeGame()) return true;
        return gameManager.isPlayerOneTurn();
    }

    public void getMoveOrOptionalPath(float x, float y) {

        List<Point> movePawnPath = gameManager.getMovePawnPath(x, y);
        if (movePawnPath != null) {
            isNeedBlock.postValue(true);
            movePawn.postValue(movePawnPath);
            gameManager.actionAfterPublishMovePawnPath();
        } else {
            List<DataCellViewClick> optionalPathByCell = gameManager.createOptionalPathByCell(x, y);
            if (optionalPathByCell != null) optionalPath.postValue(optionalPathByCell);
        }
    }

    public void removePawnIfNeeded() {
        PawnDataImpl pawnData = gameManager.removePawnIfNeeded();
        if (pawnData!=null){
            removePawn.postValue(pawnData.getStartXY());
            gameManager.updatePawnKilled();
        }

    }

    public boolean isQueenPawn(Point currPawnPoint) {
        return gameManager.isQueenPawn(currPawnPoint);
    }

    public Point getPointPawnByCell(Point pointByCell) {
        return gameManager.getPointPawnByCell(pointByCell);
    }

    public void finishTurn() {
        isNeedBlock.postValue(false);
        nextTurn.postValue(true);
    }
}
