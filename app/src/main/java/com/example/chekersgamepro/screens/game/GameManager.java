package com.example.chekersgamepro.screens.game;

import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.ai.DataGameBoard;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.move.Move;
import com.example.chekersgamepro.data.move.RemoteMove;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsData;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsDataByPlayer;
import com.example.chekersgamepro.db.repository.RepositoryManager;
import com.example.chekersgamepro.enumber.PlayersCode;
import com.example.chekersgamepro.game_validation.GameValidationImpl;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.screens.game.model.GameFinishState;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class GameManager {

    private GameValidationImpl gameValidation;

    private GameCreatorImpl gameCreator;

    private DataGame dataGame = DataGame.getInstance();

    private int gameMode;

    private RepositoryManager repositoryManager = RepositoryManager.create();

    private RemoteMove move = new RemoteMove();

    private PublishSubject<TotalPawnsDataByPlayer> pawnsData = PublishSubject.create();

    private boolean isPlayerOneTurn;
    private boolean isYourTurn;

    private ImmutableList<DataCellViewClick> relevantCellsStart;

    public Completable initGame() {
        this.gameMode = this.dataGame.getGameMode();
        return isOwnerAsync()
                .flatMapCompletable(isOwner -> repositoryManager.getNowPlayAsync()
                        .flatMapCompletable(nowPlayer -> initPlayerTurn(isOwner, nowPlayer)));
    }

    private Completable initPlayerTurn(boolean isOwner, int nowPlayer) {
        Log.d("TEST_GAME", "GameManager -> initPlayerTurn -> isOwner: " + isOwner);
        if (GameManager.this.gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
            GameManager.this.isPlayerOneTurn = PlayersCode.PLAYER_ONE.ordinal() == nowPlayer;
            GameManager.this.isYourTurn = !isOwner;
        } else {
            GameManager.this.isPlayerOneTurn = true;
            GameManager.this.isYourTurn = false;
        }

        GameManager.this.dataGame.setPlayerTurn(isPlayerOneTurn);

        gameCreator = new GameCreatorImpl();

        gameValidation = new GameValidationImpl(null);
        return Completable.complete();
    }

    public List<BorderLine> getBorderLines() {
        return dataGame.getBorderLines();
    }

    public int getColorBorderCell() {
        return dataGame.getColorBorderCell();
    }

    public int getBorderWidth() {
        return dataGame.getBorderWidth();
    }

    public Map<Point, PawnDataImpl> getPawns() {
        return dataGame.getPawns();
    }

    public Map<Point, CellDataImpl> getCells() {
        return dataGame.getCells();
    }

    public void createRelevantCellsStart() {
        relevantCellsStart = gameCreator.createRelevantCellsStart();
    }

    public void nextTurnChangePlayer() {

        gameCreator.clearData();

        isPlayerOneTurn = !isPlayerOneTurn;
        isYourTurn = !isYourTurn;

        dataGame.setPlayerTurn(isPlayerOneTurn);

    }

    public List<DataCellViewClick> createOptionalPathByCell(Point point) {
        CellDataImpl cellByPoint = dataGame.getCellByPoint(point);
        move.setIdStartCell(cellByPoint.getIdCell());
        return gameCreator.createOptionalPath(cellByPoint);
    }

    public List<Point> getMovePawnPath(Point endPoint) {
        List<Point> movePawnPath = gameCreator.getMovePawnPath(endPoint);
        if (movePawnPath != null) {
            move.setIdEndCell(dataGame.getCellByPoint(endPoint).getIdCell());
        }
        return movePawnPath;
    }

    public PawnDataImpl removePawnIfNeeded() {
        return gameCreator.removePawnIfNeeded();
    }

    public void updatePawnKilled() {
        gameCreator.updatePawnKilled();
    }

    private Single<TotalPawnsDataByPlayer> createTotalPawnsData() {

        int queenPawnsPlayerOne = dataGame.getPawnsKingPlayerOne();
        int regularPawnsPlayerOne = dataGame.getTotalRegularPawnsPlayerOne();

        int queenPawnsPlayerTwo = dataGame.getPawnsKingPlayerTwo();
        int regularPawnsPlayerTwo = dataGame.getTotalRegularPawnsPlayerTwo();

        return Single.just(new TotalPawnsDataByPlayer(
                new TotalPawnsData(regularPawnsPlayerOne, queenPawnsPlayerOne),
                new TotalPawnsData(regularPawnsPlayerTwo, queenPawnsPlayerTwo)));
    }

    public void actionAfterPublishMovePawnPath() {
        gameCreator.actionAfterPublishMovePawnPath();
    }

    public Point getPointPawnByCell(Point pointByCell) {
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(dataGame.getCellByPoint(pointByCell).getPointStartPawn());
        return pawnByPoint != null ? pawnByPoint.getStartXY() : null;
    }

    /**
     * Check if the current pawn is a queen
     *
     * @param currPawnPoint
     * @return
     */
    public boolean isQueenPawn(Point currPawnPoint) {
        return gameValidation.isQueenPawn(dataGame.getCellByPoint(currPawnPoint));
    }

    public boolean isYourWin() {
        return !isYourTurn;
    }

    public boolean isComputerGameMode() {
        return dataGame.getGameMode() == DataGame.Mode.COMPUTER_GAME_MODE;
    }

    public boolean isOnlineGameMode() {
        return dataGame.getGameMode() == DataGame.Mode.ONLINE_GAME_MODE;
    }

    public boolean isYourTurn() {
        return isYourTurn;
    }

    public boolean isComputerTurn() {
        return isComputerGameMode() && isPlayerOneTurn;
    }

    public GameFinishData isFinishGameSetWinnerPlayer(GameFinishState gameFinishState) {
        Optional<Boolean> isYourWin;
        boolean isNeedUpdateUserProfile = isOnlineGameMode();

        if (gameFinishState.ordinal() == GameFinishState.NORMAL_STATE.ordinal()) {
            isYourWin = isFinishGame() ? Optional.of(isYourWin()) : Optional.absent();
        } else {
            isYourWin = Optional.of(gameFinishState.ordinal() == GameFinishState.TECHNICAL_WIN.ordinal());
        }

        return new GameFinishData(isYourWin, isNeedUpdateUserProfile);
    }

    public boolean isFinishGame() {
        return relevantCellsStart.size() == 0;
    }

    public ImmutableList<DataCellViewClick> getRelevantCellsStart() {
        return relevantCellsStart;
    }

    public Completable notifyPawnsDataChange() {
        return createTotalPawnsData()
                .doOnEvent(this::notifyPawnsDataChange)
                .ignoreElement();
    }

    private void notifyPawnsDataChange(TotalPawnsDataByPlayer totalPawnsDataByPlayer, Throwable throwable) {
        pawnsData.onNext(totalPawnsDataByPlayer);
    }

    public Completable notifyEndTurn() {
        return isYourTurn && isOnlineGameMode() ? repositoryManager.notifyEndTurn(move) : Completable.complete();
    }

    private PublishSubject<Move> getMoveAI = PublishSubject.create();


    public void createMoveAI() {
        Move bestMove = new DataGameBoard().getBestMove();
        getMoveAI.onNext(bestMove);
    }

    public Observable<Move> getRemoteMove() {
        Observable<Move> remoteMoveChanges =
                repositoryManager
                        .getRemoteMove()
                        .startWith(new RemoteMove())
                        .map(this::convertRemoteMoveToMove);

        Observable<Move> aiMoves = getMoveAI.hide()
                .flatMap(move -> {
                    Long computerTime = move.getComputerTime();
                    Log.d("TEST_GAME", "nextInt: " + computerTime);
                    return Observable.fromCallable(() -> move)
                            .delay(computerTime, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread());
                })
                .startWith(new Move());

        return Observable.combineLatest(remoteMoveChanges, aiMoves
                , (remoteMove, move) -> isOnlineGameMode() ? remoteMove : move)
                .filter(ignored -> !isYourTurn);
    }

    private Move convertRemoteMoveToMove(RemoteMove remoteMove) {

        Point pointStartCellById = dataGame.getPointCellById(remoteMove.getIdStartCell());
        Point pointEndCellById = dataGame.getPointCellById(remoteMove.getIdEndCell());

        return new Move(pointStartCellById, pointEndCellById);
    }

    public boolean isPlayerOneTurn() {
        return isPlayerOneTurn;
    }

    public Observable<TotalPawnsDataByPlayer> getTotalPawnsChanges() {
        return pawnsData.hide();
    }

    public void pingRemotePlayer() {

    }

    public Completable setFinishGameTechnicalLoss() {
        return isOnlineGameMode()
                ? repositoryManager.setFinishGameTechnicalLoss()
                : Completable.complete();
    }

    public Observable<Boolean> isTechnicalWin() {
        int onlineGameMode = DataGame.Mode.ONLINE_GAME_MODE;

        return repositoryManager.isTechnicalWin()
                .filter(ignored -> gameMode == onlineGameMode);
    }

    public Observable<Boolean> isOwnerAsync() {
        return repositoryManager.isOwnerPlayerAsync();
    }
}
