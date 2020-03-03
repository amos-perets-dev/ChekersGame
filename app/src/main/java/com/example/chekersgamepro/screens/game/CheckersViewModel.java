package com.example.chekersgamepro.screens.game;

import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsDataByPlayer;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.screens.game.model.GameFinishState;
import com.example.chekersgamepro.util.CheckersApplication;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.PublishSubject;

public class CheckersViewModel extends ViewModel {

    private MutableLiveData<ImmutableList<DataCellViewClick>> checkRelevantCellsStart = new MutableLiveData<>();

    private MutableLiveData<Boolean> isPlayerOneTurn = new MutableLiveData<>();

    private MutableLiveData<List<DataCellViewClick>> optionalPath = new MutableLiveData<>();

    private MutableLiveData<List<Point>> movePawn = new MutableLiveData<>();

    private MutableLiveData<Point> removePawn = new MutableLiveData<>();

    private MutableLiveData<GameFinishData> isFinishGame = new MutableLiveData<>();

    private MutableLiveData<Pair<Point, Boolean>> computerOrRemotePlayerTurn = new MutableLiveData<>();

    private MutableLiveData<Boolean> nextTurn = new MutableLiveData<>();

    private PublishSubject<Boolean> finishCheckedOptionalPath = PublishSubject.create();

    private GameManager gameManager = new GameManager() ;

    private Observable<Boolean> isfFinishCheckedOptionalPath(){
        return finishCheckedOptionalPath.hide();
    }

    public Observable<Pair<Point, Boolean>> getComputerOrRemotePlayerMove(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerOrRemotePlayerTurn))
                .filter(pairMove -> pairMove != null && pairMove.first != null && pairMove.first.x != 0);
    }

    public Observable<Boolean> getNextTurn(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, nextTurn));
    }

    public Observable<GameFinishData> isFinishGame(LifecycleOwner lifecycleOwner){
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, isFinishGame))
                .filter(gameFinishData -> gameFinishData.isYourWin().isPresent());
    }

    public Observable<List<Point>> getMovePawn(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, movePawn));
    }

    public Observable<ImmutableList<DataCellViewClick>> getRelevantCells(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, checkRelevantCellsStart));
    }

    public Observable<Boolean> getPlayerName(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, isPlayerOneTurn));
    }

    public Observable<List<DataCellViewClick>> getOptionalPath(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, optionalPath));
    }

    public Observable<Point> removePawn(LifecycleOwner lifecycleOwner) {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, removePawn));
    }

    public void initGame(int x, int y, int width, int height, int gameMode, String playerOne, String playerTwo) {
        gameManager.initGame(x, y, width, height, gameMode, playerOne, playerTwo);
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


    public void initStartGameOrNextTurn(){

        isPlayerOneTurn.postValue(gameManager.isPlayerOneTurn());

        gameManager.createRelevantCellsStart();

        setFinishGame(GameFinishState.NORMAL_STATE);

        checkRelevantCellsStart.postValue(gameManager.getRelevantCellsStart());

        // Check if it's computer turn
        // and if it computer turn need to create move
        if (gameManager.isComputerTurn()) gameManager.createMoveAI();

    }

    public void nextTurn() {
        gameManager.nextTurnChangePlayer();
        initStartGameOrNextTurn();
    }

    public void setFinishGame(GameFinishState gameFinishState){
        isFinishGame.postValue(gameManager.isFinishGameSetWinnerPlayer(gameFinishState));
    }

    public Completable setFinishGameTechnicalLoss(GameFinishState gameFinishState){
        return gameManager.setFinishGameTechnicalLoss()
                .doOnEvent(throwable -> setFinishGame(gameFinishState));
    }

    public Observable<GameFinishState> isTechnicalWin(){
        return gameManager.isTechnicalWin()
                .filter(Functions.equalsWith(true))
                .map(ignored -> GameFinishState.TECHNICAL_WIN)
                .doOnNext(this::setFinishGame);
    }

    public boolean isYourTurn(){
        return gameManager.isYourTurn();
    }

    public void showErrorMsg(float x, float y) {

        Point point = new Point((int) x, (int) y);

        boolean isInvalidTurn = !gameManager.isYourTurn() || gameManager.isComputerTurn();
        if (isInvalidTurn){
            CheckersApplication.create().showToast("ERROR");
            return;
        }

        setMoveOrOptionalPath(point);
    }

    public void setMoveOrOptionalPath(Point point) {
        Log.d("TEST_GAME", " public void setMoveOrOptionalPath(Point point): " + point);
        boolean isCreateMovePawnPath = createMovePawnPath(point);

        if (!isCreateMovePawnPath){
            createOptionalPathByCell(point);
        }
    }

    private boolean createMovePawnPath(Point point) {
        List<Point> movePawnPath = gameManager.getMovePawnPath(point);
        if (movePawnPath == null) return false;
        movePawn.postValue(movePawnPath);
        gameManager.actionAfterPublishMovePawnPath();
        return true;
    }

    private void createOptionalPathByCell(Point point){
        List<DataCellViewClick> optionalPathByCell = gameManager.createOptionalPathByCell(point);
        if (optionalPathByCell != null) {
            optionalPath.postValue(optionalPathByCell);
        }
    }

    public void removePawnIfNeeded(Boolean isStartIterateMovePawn) {
        if (isStartIterateMovePawn){
            PawnDataImpl pawnData = gameManager.removePawnIfNeeded();
            if (pawnData!=null){
                removePawn.postValue(pawnData.getStartXY());
                gameManager.updatePawnKilled();
            }
        }
    }

    public boolean isQueenPawn(Point currPawnPoint) {
        return gameManager.isQueenPawn(currPawnPoint);
    }

    public Point getPointPawnByCell(Point pointByCell) {
        return gameManager.getPointPawnByCell(pointByCell);
    }

    public Disposable getRemoteMove() {
        return gameManager.getRemoteMove()
                .distinctUntilChanged()
                .doOnNext(move -> setComputerOrRemotePlayerMove(move.getStartPoint(), false))
                .flatMap(move -> isfFinishCheckedOptionalPath()
                        .doOnNext(ignored -> setComputerOrRemotePlayerMove(move.getEndPoint(), true)))
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("TEST_GAME", "CheckersViewModel -> doOnDispose -> Remote move");
                    }
                })
                .subscribe();
    }

    private void setComputerOrRemotePlayerMove(Point point, boolean isNeedBackAfterClick){
        computerOrRemotePlayerTurn.postValue(new Pair<>(point, isNeedBackAfterClick));
    }

    public Completable finishTurn() {
        return gameManager.notifyEndTurn()
                .andThen(gameManager.notifyPawnsDataChange())
                .doOnEvent(throwable -> nextTurn.postValue(true));
    }

    public Completable finishCheckedOptionalPath() {
        if (!gameManager.isYourTurn()) finishCheckedOptionalPath.onNext(true);
        return Completable.complete();
    }

    public boolean isTopPlayer() {
        return gameManager.isPlayerOneTurn();
    }

    public Observable<TotalPawnsDataByPlayer> getTotalPawnsChanges() {
        return gameManager.getTotalPawnsChanges();
    }

    public void notifyTimeOver() {
        gameManager.pingRemotePlayer();
    }

}
