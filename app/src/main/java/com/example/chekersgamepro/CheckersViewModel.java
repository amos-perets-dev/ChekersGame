package com.example.chekersgamepro;

import android.graphics.Point;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;

public class CheckersViewModel extends ViewModel {

    private MutableLiveData<Boolean> initFinish = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> relevantCells = new MutableLiveData<>();

    private MutableLiveData<String> playerName = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> optionalPath = new MutableLiveData<>();

    private MutableLiveData<List<Point>> movePawn = new MutableLiveData<>();

    private MutableLiveData<Point> removePawn = new MutableLiveData<>();

    private MutableLiveData<String> winPlayer = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> computerTurn = new MutableLiveData<>();

    private GameManager gameManager;

    public CheckersViewModel() {

        this.gameManager = new GameManager();

    }

    public Observable<List<DataCellViewClick>> getComputerStartTurn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerTurn));
    }

    public Observable<String> getWinPlayerName(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, winPlayer));
    }

    public Observable<List<Point>> getMovePawn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, movePawn));
    }

    public Observable<Boolean> initFinish(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, initFinish));
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
        initFinish.postValue(true);
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
        List<DataCellViewClick> relevantCellsStart = gameManager.createRelevantCellsStart();
        relevantCells.postValue(relevantCellsStart);

        if (gameManager.isSomePlayerWin()){
            winPlayer.postValue(gameManager.getWinPlayerName());
        }

        if (gameManager.isComputerModeGame()){
            if (!gameManager.isPlayerOneTurn()){
                computerTurn.postValue(relevantCellsStart);
            } else {
                computerTurn.postValue(Collections.EMPTY_LIST);
            }
        }

    }

    public boolean isClickableViews(){
        return !(gameManager.isComputerModeGame() && !gameManager.isPlayerOneTurn());
    }

    public Set<Point> getOptionalPointsListComputer(){
        return gameManager.getOptionalPointsListComputer();
    }

    public void getMoveOrOptionalPath(float x, float y) {

        List<Point> movePawnPath = gameManager.getMovePawnPath(x, y);
        if (movePawnPath != null) {
            movePawn.postValue(movePawnPath);
            gameManager.actionAfterPublishMovePawnPath();
        } else {
            List<DataCellViewClick> optionalPathByCell = gameManager.createOptionalPathByCell(x, y);
            optionalPath.postValue(optionalPathByCell == null ? Collections.EMPTY_LIST : optionalPathByCell);
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
}
