package com.example.chekersgamepro;

import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class CheckersViewModel extends ViewModel {

    private MutableLiveData<Boolean> initFinish = new MutableLiveData<>();

    private MutableLiveData<Boolean> initRelevantCellsFinish = new MutableLiveData<>();

    private MutableLiveData<List<Point>> relevantCells = new MutableLiveData<>();

    private MutableLiveData<String> playerName = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> optionalPath = new MutableLiveData<>();

    private MutableLiveData<List<Point>> movePawn = new MutableLiveData<>();

    private MutableLiveData<Point> removePawn = new MutableLiveData<>();

    private GameManager gameManager;

    public CheckersViewModel() {

        this.gameManager = new GameManager();

    }

    public Observable<List<Point>> getMovePawn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, movePawn));
    }

    public Observable<Boolean> initFinish(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, initFinish));
    }

    public Observable<List<Point>> getRelevantCells(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, relevantCells));
    }

    public Observable<String> getPlayerName(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, playerName));
    }

    public Observable<Boolean> startTurn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, initRelevantCellsFinish));
    }

    public Observable<List<DataCellViewClick>> getOptionalPath(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, optionalPath));
    }

    public Observable<Point> removePawn(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, removePawn));
    }

    public void initGame(int x, int y, int width, int height) {
        gameManager.initGame(x, y, width, height);
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

    public void finishedCheckedRelevantCells() {
        initRelevantCellsFinish.postValue(true);
    }


    public void nextTurn() {
        gameManager.clearData();
        playerName.postValue(gameManager.nextTurnChangePlayer());
        relevantCells.postValue(gameManager.createRelevantCellsStart());
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

    public Point getPointPawnByCell(Point pointByCell) {

        return gameManager.getPointPawnByCell(pointByCell);

    }

    public boolean isKingPawn(Point currPoint) {

        return gameManager.isKingPawn(currPoint);

    }
}
