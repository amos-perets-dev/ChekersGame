package com.example.chekersgamepro.screens.game;

import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.ai.DataGameBoard;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.game_board.GameInitialImpl;
import com.example.chekersgamepro.data.move.Move;
import com.example.chekersgamepro.data.move.RemoteMove;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsData;
import com.example.chekersgamepro.data.pawn.total.TotalPawnsDataByPlayer;
import com.example.chekersgamepro.db.repository.RepositoryManager;
import com.example.chekersgamepro.enumber.PlayersCode;
import com.example.chekersgamepro.game_validation.GameValidationImpl;
import com.example.chekersgamepro.models.player.IPlayer;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.screens.game.model.GameFinishState;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class GameManager {

    private GameInitialImpl gameInitialImpl;

    private GameValidationImpl gameValidation;

    private GameCreatorImpl gameCreator;

    private DataGame dataGame = DataGame.getInstance();

    private String playerOne;
    private String playerTwo;

    private int gameMode;

    private RepositoryManager repositoryManager = RepositoryManager.create();

    private RemoteMove move = new RemoteMove();

    private PublishSubject<TotalPawnsDataByPlayer> pawnsData = PublishSubject.create();

    private boolean isPlayerOneTurn;
    private boolean isYourTurn;
    private boolean isOwner = repositoryManager.getPlayer().isOwner();

    private boolean isInitGame = false;

    private ImmutableList<DataCellViewClick> relevantCellsStart;

    public void initGame(int x, int y, int width, int height, int gameMode, String playerOne, String playerTwo) {
        this.gameMode = gameMode;

        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
            this.isPlayerOneTurn = PlayersCode.PLAYER_ONE.ordinal() == repositoryManager.getPlayer().getNowPlay();
            this.isYourTurn = !isOwner;
        } else {
            this.isPlayerOneTurn = true;
            this.isYourTurn = false;
        }

        this.dataGame.setPlayerTurn(isPlayerOneTurn);

        gameInitialImpl = new GameInitialImpl(x, y, width, height, gameMode);

        gameCreator = new GameCreatorImpl();

        gameValidation = new GameValidationImpl(null);
        isInitGame = true;
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

    public String getPlayerName() {

        if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
            return isYourTurn ? repositoryManager.getPlayer().getPlayerName() : repositoryManager.getPlayer().getRemotePlayer();
        }

        return isYourTurn ? playerTwo : playerOne;

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

    public String getWinPlayerName() {

        String baseText = "The winning player is";

        if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
            IPlayer player = RepositoryManager.create().getPlayer();
            return baseText + " " + (isYourTurn ? player.getRemotePlayer() : player.getPlayerName()) + " you " + (isYourTurn ? "LOOSE" : "WIN");
        }

        return baseText + " " + (isYourTurn ? playerOne : playerTwo);
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

    public boolean isFinishGame(){
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

        Observable<Move> aiMoves = getMoveAI.hide().startWith(new Move());

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

        return  repositoryManager.isTechnicalWin()
                .filter(ignored -> gameMode == onlineGameMode);
    }

    public boolean isValid() {
        return gameCreator != null;
    }

//    public void resetRemoteMove() {
//        RepositoryManager.create().resetRemoteMove();
//    }
}
