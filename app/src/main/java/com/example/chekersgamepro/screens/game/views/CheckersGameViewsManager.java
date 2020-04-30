package com.example.chekersgamepro.screens.game.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.game_board.GameInitialImpl;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.example.chekersgamepro.models.player.game.PlayerGame;
import com.example.chekersgamepro.screens.game.CheckersViewModel;
import com.example.chekersgamepro.screens.game.StartGameDialog;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.checkers.CheckersApplication;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;

public class CheckersGameViewsManager {

    private TextView textViewTestStart;

    private final CheckersViewModel checkersViewModel;

    private ComputerIconView computerIconView;

    private View gameBoardContainer;

    private PlayersNamesView playersNamesView;

    private ImmutableList<CellView> viewsByRelevantCellsList;

    private ProgressTimeView timerView;

    private GameBoardViews gameBoardViews;

    private StartGameDialog startGameDialog;

    private int gameMode;

    private GameBoardView gameBoardView;

    public CheckersGameViewsManager(Activity activity, CheckersViewModel checkersViewModel, CompositeDisposable compositeDisposable, Intent intent) {
        this.checkersViewModel = checkersViewModel;
        findViews(activity);

        gameMode   = intent.getIntExtra("GAME_MODE", DataGame.Mode.COMPUTER_GAME_MODE);

        PlayerGame playerOne = (PlayerGame) intent.getSerializableExtra("PLAYER_ONE");
        PlayerGame playerTwo = (PlayerGame) intent.getSerializableExtra("PLAYER_TWO");

        gameBoardView = activity.findViewById(R.id.game_board_view);

        startGameDialog     = new StartGameDialog(activity, playerOne, playerTwo, gameMode == DataGame.Mode.COMPUTER_GAME_MODE);

        computerIconView    = new ComputerIconView(activity, gameBoardView, checkersViewModel.isOwnerAsync());
        playersNamesView    = new PlayersNamesView(activity, playerOne.getPlayerNme(), playerTwo.getPlayerNme());
        gameBoardViews      = new GameBoardViews(gameBoardView, checkersViewModel, activity);
        timerView           = new ProgressTimeView(activity, checkersViewModel);

        compositeDisposable.add(
                checkersViewModel.getTotalPawnsChanges()
                        .subscribe(playersNamesView::setData)
        );

    }

    public Observable<Object> initGame(){
        return gameBoardView.onDrawFinish()
                .distinctUntilChanged()
                .doOnNext(new Consumer<GameBoardView>() {
                    @Override
                    public void accept(GameBoardView gameBoardView) throws Exception {
                        startGameDialog.showDialogGame();
                    }
                })
                .flatMap(gameBoardView -> startGameDialog.isShowDialogGame()
                        .flatMap(new Function<Boolean, ObservableSource<?>>() {
                                     @Override
                                     public ObservableSource<?> apply(Boolean isShowDialogGame) throws Exception {
                                         Completable completable = isShowDialogGame ? CheckersGameViewsManager.this.initViews() : Completable.complete();
                                         return completable
                                                 .andThen(isShowDialogGame ? Completable.complete() : CheckersGameViewsManager.this.showViews())
                                                 .andThen(Observable.just(isShowDialogGame))
                                                 .filter(Functions.equalsWith(false));
                                     }
                                 }
                        )
                );
    }

    private Completable initViews() {
        return initGameBoardCellsPawns(gameMode)
                .andThen(gameBoardViews.initCellsAndPawns())
                .andThen(computerIconView.initComputerIcon(gameMode, timerView.getHeight()));

    }

    private void findViews(Activity activity) {
        gameBoardContainer = activity.findViewById(R.id.container_game_board);
        textViewTestStart = activity.findViewById(R.id.text_test_start);
    }

    public void setPlayerTurn(boolean isPlayerOneTurn) {
        playersNamesView.setPlayerTurn(isPlayerOneTurn);
    }

    public List<Observable<? extends View>> addViewsToObservable(Object ignored) {
        return gameBoardViews.addViewsToObservable();
    }

//    /**
//     * Update the pawn view by point when the animate move finished
//     *
//     * @param point the path end point
//     */
    private void updatePawnViewStart(Point point, Point currPointPawnViewStartPath, PawnView currPawnViewStartPath) {
        gameBoardViews.updatePawnViewStart(point, currPointPawnViewStartPath, currPawnViewStartPath);
    }

    public PawnView getPawn(Point pointPawnByCell) {
        return gameBoardViews.getPawn(pointPawnByCell);
    }

    public void removePawnView(Point point) {
        gameBoardViews.removePawnView(point);
    }

    public void checkedCell(Point point, int color) {
         gameBoardViews.checkedCell(point, color);
    }

    private Completable initGameBoardCellsPawns(int gameMode) {
        return  new GameInitialImpl(
                (int) gameBoardView.getX()
                , (int) gameBoardView.getY()
                , gameBoardView.getMeasuredWidth()
                , gameBoardView.getMeasuredHeight()
                , gameMode)
                .init()
                .andThen(checkersViewModel.initGame());
    }

    private void clearCheckedCellsPath(List<DataCellViewClick> cellsViewOptionalPath) {
        for (DataCellViewClick cellViewClick : cellsViewOptionalPath) {
            gameBoardViews.clearCheckedCell(cellViewClick.getPoint());
        }
    }

    public Completable animateComputerIconView(Pair<Point, Boolean> pair) {
        if (pair == null) return Completable.error(new Throwable("pair == null"));

        Point pointCell = pair.first;
        return computerIconView.animateComputerIcon(pair.second, pointCell, gameBoardViews.getCellViewByPoint(pointCell))
                .doOnEvent(throwable -> checkersViewModel.setMoveOrOptionalPath(pair.first));
    }

    public Completable showViews() {
        return Completable.create(emitter -> gameBoardContainer
                .animate()
                .withLayer()
                .alpha(1)
                .setDuration(200)
                .withEndAction(emitter::onComplete)
                .start())
                .andThen(playersNamesView.showViewsWithAnimate())
                .andThen(computerIconView.showWithAnimate());
    }

    public void setFinishGame(GameFinishData finishGame) {
        timerView.dispose();
        String winOrLoose = finishGame.getWinOrLoose();
        textViewTestStart.setText(winOrLoose);
        CheckersApplication.create().showToast(winOrLoose);
    }

    /**
     * Add the views of the relevant cells start to the list
     *
     * @param cellsViewList list of the relevant cells start
     */
    public void addViewsByRelevantCells(ImmutableList<DataCellViewClick> cellsViewList) {
        // add the relevant cells to the list
        viewsByRelevantCellsList = FluentIterable.from(cellsViewList)
                .transform(DataCellViewClick::getPoint)
                .transform(gameBoardViews::getCellViewByPoint)
                .toList();
    }

    /**
     * Clear the views list of the prev relevant cells
     */
    private void clearPrevRelevantCellsStart() {
        for (CellView cellView : viewsByRelevantCellsList){
            cellView.checked(DataGame.ColorCell.CLEAR_CHECKED);
        }
    }

    public void setTimerState(Boolean isStartTimer) {
        if (isStartTimer){
            timerView.startTimer();
        } else {
            timerView.stopTimer();
        }
    }

    public Completable setEndTurn(List<DataCellViewClick> cellsViewOptionalPath, Point lastPointPath, Point pointPawnStartPath, PawnView pawnViewStartPath) {
        updatePawnViewStart(lastPointPath, pointPawnStartPath, pawnViewStartPath);
        // clear the optional checked path after the turn finished
        clearCheckedCellsPath(cellsViewOptionalPath);
        clearPrevRelevantCellsStart();
        return Completable.complete();
    }
}
