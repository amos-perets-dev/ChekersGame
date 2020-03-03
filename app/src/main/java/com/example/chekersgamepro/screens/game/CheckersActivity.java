package com.example.chekersgamepro.screens.game;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.screens.game.model.GameFinishState;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class CheckersActivity extends AppCompatActivity {

    private CheckersViewModel checkersViewModel;

    private List<CellView> viewsByRelevantCellsList = new ArrayList<>();

    private CompositeDisposable compositeDisposable;

    private Disposable disposableAnimatePawnMove = Disposables.disposed();

    private Disposable disposableFinishGame = Disposables.disposed();
    private Disposable disposableRemoteMove = Disposables.disposed();

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

        Log.d("TEST_GAME", "CheckersActivity -> onCreate: " + this.toString());
        compositeDisposable = new CompositeDisposable();
        checkersViewModel = ViewModelProviders.of(this).get(CheckersViewModel.class);

        checkersGameViewsManager = new CheckersGameViewsManager(this, checkersViewModel, compositeDisposable);

        // Open dialog to choose game mode
        // init the game board, pawns and cells
        // create observables to the views
        StartGameDialog startGameDialog = new StartGameDialog(this, getIntent());
        compositeDisposable.add(startGameDialog.getGameMode()
                .doOnNext(ignored -> checkersGameViewsManager.initViews(getIntent()))
                .doOnNext(Functions.actionConsumer(checkersGameViewsManager::showViews))
                .doOnError(Throwable::printStackTrace)
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnNext(Functions.actionConsumer(checkersViewModel::initStartGameOrNextTurn))
                .observeOn(AndroidSchedulers.mainThread())
                .map(checkersGameViewsManager::addViewsToObservable)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .doOnNext(view -> checkersGameViewsManager.setTest(view))
                .subscribe(this::onClickCell));


        changePlayerNameAsync();

        startTurnCheckedCellsStartAsync();

        checkedOptionalPathAsync();

        animateMovePawnAsync();

        nextTurnAsync();

        removePawnsAsync();

        showFinishGameAsync();

        compositeDisposable.add(checkersViewModel.getRemoteMove());


        compositeDisposable.add(checkersViewModel.getComputerOrRemotePlayerMove(this)
                .flatMapCompletable(checkersGameViewsManager::animateComputerIconView)
                .subscribe()
        );

        compositeDisposable.add(checkersViewModel.isTechnicalWin().subscribe());
    }

    /**
     * Show the player name winning
     */
    private void showFinishGameAsync() {
        disposableFinishGame = checkersViewModel
                .isFinishGame(this)
                .doOnNext(checkersGameViewsManager::setFinishGame)
                .delay(2500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(GameFinishData::getIntentBackToHomePage)
                .flatMapCompletable(this::setResult)
                .subscribe();
    }

    /**
     * Remove the relevant pawns views that need to be killed
     */
    private void removePawnsAsync() {
        compositeDisposable.add(checkersViewModel
                .removePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checkersGameViewsManager::removePawnView));
    }

    /**
     * Set the next turn
     */
    private void nextTurnAsync() {
        compositeDisposable.add(checkersViewModel.getNextTurn(this)
                .observeOn(Schedulers.io())
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

    }

    /**
     * Animate the moving pawn, that clicked by the user
     */
    private void animateMovePawnAsync() {
        compositeDisposable.add(checkersViewModel
                .getMovePawn(this)
                .subscribeOn(Schedulers.io())
                .doOnNext(pointsListAnimatePawn -> {
                    Point pointPawnStartPath = pointsListAnimatePawn.get(0);
                    animatePawnMove(pointsListAnimatePawn, pointPawnStartPath);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    /**
     * Checked the optional path by click
     * And set the pawn start view
     */
    private void checkedOptionalPathAsync() {
        compositeDisposable.add(checkersViewModel
                .getOptionalPath(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(dataCellViewClicks -> {
                    // set the pawn start in the current path
                    pawnViewStartPath = checkersGameViewsManager.getPawn(checkersViewModel.getPointPawnByCell(dataCellViewClicks.get(0).getPoint()));
                })
                .flatMapCompletable(this::checkedOptionalPathByClick)
                .subscribe());
    }

    /**
     * Get the relevant cells start and checked them
     * And if needed (if it is the computer turn) set the clickable to the false
     */
    private void startTurnCheckedCellsStartAsync() {
        compositeDisposable.add(checkersViewModel
                .getRelevantCells(this)
                .doOnNext(this::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .doOnNext(this::checkRelevantCellsStart)
                .doOnNext(Functions.actionConsumer(checkersGameViewsManager::notifyProgress))
                .subscribe());
    }

    /**
     * Change the current player name
     */
    private void changePlayerNameAsync() {
        compositeDisposable.add(checkersViewModel
                .getPlayerName(this)
                .subscribe(checkersGameViewsManager::setPlayerTurn));
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
                    CheckersActivity.this.cellsViewOptionalPath = new ArrayList<>(dataCellViewClicks);
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
     * Clear the views list of the prev relevant cells
     */
    private void clearPrevRelevantCellsStart() {

        FluentIterable.from(viewsByRelevantCellsList)
                .transform(cellView -> cellView.checked(DataGame.ColorCell.CLEAR_CHECKED))
                .toList();

        viewsByRelevantCellsList.clear();
    }

    /**
     * Add the views of the relevant cells start to the list
     *
     * @param cellsViewList list of the relevant cells start
     */
    private void addViewsByRelevantCells(ImmutableList<DataCellViewClick> cellsViewList) {

        // add the relevant cells to the list
        FluentIterable.from(cellsViewList)
                .transform(DataCellViewClick::getPoint)
                .transform(checkersGameViewsManager::getCellViewByPoint)
                .transform(viewsByRelevantCellsList::add)
                .toList();

    }

    /**
     * Set the end turn, clear the lists and set the relevant pawns
     *
     * @param lastPointPath      of the current path that choose by the user
     * @param pointPawnStartPath of the current path that choose by the user
     * @return
     */
    private Completable endTurn(Point lastPointPath, Point pointPawnStartPath) {
        checkersGameViewsManager.updatePawnViewStart(lastPointPath, pointPawnStartPath, pawnViewStartPath);

        // clear the optional checked path after the turn finished
        checkersGameViewsManager.clearCheckedCellsList(cellsViewOptionalPath);
        clearPrevRelevantCellsStart();
        cellsViewOptionalPath.clear();
        return checkersViewModel.finishTurn()
                .andThen(checkersGameViewsManager.resetProgress());
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
        Log.d("TEST_GAME", "CheckersActivity -> setResult");
        setResult(111, intent);
        finish();
        return Completable.complete();
    }

    @Override
    protected void onDestroy() {
        Log.d("TEST_GAME", "CheckersActivity -> onDestroy");

//        checkersViewModel.resetRemoteMove();

        compositeDisposable.dispose();
        disposableAnimatePawnMove.dispose();
        disposableFinishGame.dispose();
        disposableRemoteMove.dispose();

        super.onDestroy();
    }
}