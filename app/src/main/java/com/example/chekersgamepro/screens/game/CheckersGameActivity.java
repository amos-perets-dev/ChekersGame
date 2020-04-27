package com.example.chekersgamepro.screens.game;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.screens.game.model.GameFinishState;
import com.example.chekersgamepro.screens.game.views.CheckersGameViewsManager;
import com.example.chekersgamepro.screens.homepage.HomePageActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class CheckersGameActivity extends AppCompatActivity {

    private CheckersViewModel checkersViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Disposable disposableAnimatePawnMove = Disposables.disposed();

    /**
     * List of the all optional path
     */
    private List<DataCellViewClick> cellsViewOptionalPath = new ArrayList<>();

    private PawnView pawnViewStartPath;

    private CheckersGameViewsManager checkersGameViewsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkersViewModel = ViewModelProviders.of(this).get(CheckersViewModel.class);

        checkersGameViewsManager = new CheckersGameViewsManager(this, checkersViewModel, compositeDisposable, getIntent());

        initAllSubscribers();

    }

    private void initAllSubscribers() {
        compositeDisposable.addAll(
                startGameAsync(),
                changePlayerNameAsync(),
                startTurnCheckedCellsStartAsync(),
                checkedOptionalPathAsync(),
                animateMovePawnAsync(),
                nextTurnAsync(),
                removePawnsAsync(),
                showFinishGameAsync(),
                isStartTimerAsync(),
                getMoveAsync()
        );
    }

    private Disposable isStartTimerAsync() {
        return checkersViewModel
                .isStartTimer(this)
                .subscribe(checkersGameViewsManager::setTimerState);
    }

    private Disposable getMoveAsync() {
        return checkersViewModel.getMoveAsync(this)
                .flatMapCompletable(pair -> checkersViewModel.notifyStopTimer()
                        .andThen(checkersGameViewsManager.animateComputerIconView(pair)))
                .subscribe();
    }

    private Disposable startGameAsync() {
        // init the game board, pawns and cells
        // create observables to the views
        return checkersGameViewsManager.initGame()
                .doOnError(Throwable::printStackTrace)
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnNext(Functions.actionConsumer(checkersViewModel::initStartGameOrNextTurn))
                .observeOn(AndroidSchedulers.mainThread())
                .map(checkersGameViewsManager::addViewsToObservable)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .subscribe(this::onClickCell);
    }

    /**
     * Show the player name winning
     *
     * @return
     */
    private Disposable showFinishGameAsync() {
        return checkersViewModel
                .isFinishGame(this)
                .doOnNext(checkersGameViewsManager::setFinishGame)
                .delay(2500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(GameFinishData::getIntentBackToHomePage)
                .flatMapCompletable(this::setResult)
                .subscribe();
    }

    /**
     * Remove the relevant pawns views that need to be killed
     *
     * @return
     */
    private Disposable removePawnsAsync() {
        return checkersViewModel
                .removePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checkersGameViewsManager::removePawnView);
    }

    /**
     * Set the next turn
     *
     * @return
     */
    private Disposable nextTurnAsync() {
        return checkersViewModel.getNextTurn(this)
                .observeOn(Schedulers.io())
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    /**
     * Animate the moving pawn, that clicked by the user
     *
     * @return
     */
    private Disposable animateMovePawnAsync() {
        return checkersViewModel
                .getMovePawn(this)
                .subscribeOn(Schedulers.io())
                .doOnNext(pointsListAnimatePawn -> {
                    Point pointPawnStartPath = pointsListAnimatePawn.get(0);
                    animatePawnMove(pointsListAnimatePawn, pointPawnStartPath);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * Checked the optional path by click
     * And set the pawn start view
     *
     * @return
     */
    private Disposable checkedOptionalPathAsync() {
        return checkersViewModel
                .getOptionalPath(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(dataCellViewClicks -> {
                    // set the pawn start in the current path
                    pawnViewStartPath = checkersGameViewsManager.getPawn(checkersViewModel.getPointPawnByCell(dataCellViewClicks.get(0).getPoint()));
                })
                .flatMapCompletable(this::checkedOptionalPathByClick)
                .subscribe();
    }

    /**
     * Get the relevant cells start and checked them
     * And if needed (if it is the computer turn) set the clickable to the false
     *
     * @return
     */
    private Disposable startTurnCheckedCellsStartAsync() {
        return checkersViewModel
                .getRelevantCells(this)
                .doOnNext(checkersGameViewsManager::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .doOnNext(this::checkRelevantCellsStart)
                .flatMapCompletable(ignored -> checkersViewModel.notifyStartTimer())
                .subscribe();
    }

    /**
     * Change the current player name
     *
     * @return
     */
    private Disposable changePlayerNameAsync() {
        return checkersViewModel
                .getPlayerName(this)
                .subscribe(checkersGameViewsManager::setPlayerTurn);
    }

    /**
     * Checked the cells that can be start cell
     *
     * @param dataCellViewClick data for the cell start
     */
    private void checkRelevantCellsStart(DataCellViewClick dataCellViewClick) {
        if (checkersViewModel.isYourTurn()) {
            checkersGameViewsManager.checkedCell(dataCellViewClick.getPoint(), dataCellViewClick.getColorChecked());
        }
    }

    private void animatePawnMove(List<Point> pointsListAnimatePawnMove, Point pointPawnStartPath) {

        if (disposableAnimatePawnMove != null) disposableAnimatePawnMove.dispose();

        disposableAnimatePawnMove = pawnViewStartPath
                .isStartIterateMovePawn()
                .doOnSubscribe(disposable -> pawnViewStartPath.animatePawnMove(pointsListAnimatePawnMove, 0))
                .doOnNext(checkersViewModel::removePawnIfNeeded)
                .filter(Functions.equalsWith(false))
                .map(ignored -> pointsListAnimatePawnMove.size() - 1) //End point
                .map(pointsListAnimatePawnMove::get)
                .flatMapCompletable(endPoint -> endTurn(endPoint, pointPawnStartPath))
                .subscribe();
    }

    private void onClickCell(View view) {
        checkersViewModel.showErrorMsg(view.getX(), view.getY());
    }

    private Completable checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks) {

        // clear the prev cells checked
        return checkedOptionalPathByClick(cellsViewOptionalPath, false)
                // checked the current cells
                .andThen(checkedOptionalPathByClick(dataCellViewClicks, true))
                .doOnEvent(throwable -> {
                    // Init the list to be set the prev list
                    CheckersGameActivity.this.cellsViewOptionalPath = new ArrayList<>(dataCellViewClicks);
                })
                .andThen(checkersViewModel.finishCheckedOptionalPath());
    }

    private Completable checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks, boolean isChecked) {
        for (DataCellViewClick cellViewClick : dataCellViewClicks) {
            checkersGameViewsManager.checkedCell(cellViewClick.getPoint(),
                    isChecked
                            ? cellViewClick.getColorChecked()
                            : cellViewClick.getColorClearChecked());
        }

        return Completable.complete();
    }

    /**
     * Set the end turn, clear the lists and set the relevant pawns
     *
     * @param lastPointPath      of the current path that choose by the user
     * @param pointPawnStartPath of the current path that choose by the user
     * @return
     */
    private Completable endTurn(Point lastPointPath, Point pointPawnStartPath) {
        return checkersGameViewsManager.setEndTurn(cellsViewOptionalPath, lastPointPath, pointPawnStartPath, pawnViewStartPath)
                .doOnEvent(ignored -> cellsViewOptionalPath.clear())
                .andThen(checkersViewModel.finishTurn()
                        .andThen(checkersViewModel.notifyStopTimer())
                );
    }

    @Override
    public void onBackPressed() {
        compositeDisposable.add(
                checkersViewModel
                        .setFinishGameTechnicalLoss(GameFinishState.TECHNICAL_LOSS)
                        .subscribe()
        );
    }

    private Completable setResult(Intent intent) {
        setResult(HomePageActivity.FINISH_GAME, intent);
        finish();
        return Completable.complete();
    }

    @Override
    protected void onDestroy() {
        checkersGameViewsManager.setTimerState(false);
        compositeDisposable.dispose();
        disposableAnimatePawnMove.dispose();
        super.onDestroy();
    }
}