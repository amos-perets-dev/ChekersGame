package com.example.chekersgamepro;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private CheckersViewModel checkersViewModel;

    private  List<CellView> viewsByRelevantCellsList = new ArrayList<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * List of the all optional path
     */
    private List<DataCellViewClick> cellsViewOptionalPath = new ArrayList<>();

    private PawnView pawnViewStartPath;

    private GameViewsManager gameViewsManager;

    private ComputerIconView computerIconView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkersViewModel = ViewModelProviders.of(MainActivity.this).get(CheckersViewModel.class);

        gameViewsManager = new GameViewsManager(this, checkersViewModel);

        computerIconView = new ComputerIconView(gameViewsManager);

        DialogGameMode dialogGameMode = new DialogGameMode(this);

        // Open dialog to choose game mode
        // init the game board, pawns and cells
        // create observables to the views
        compositeDisposable.add(dialogGameMode
                .getGameMode()
                .doOnNext(gameViewsManager::initViews)
                .doOnNext(Functions.actionConsumer(dialogGameMode::dismiss))
                .doOnError(Throwable::printStackTrace)
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .observeOn(AndroidSchedulers.mainThread())
                .map(gameViewsManager::addViewsToObservable)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .doOnNext(view -> gameViewsManager.setTest(view))
                .subscribe(this::onClickCell));


        changePlayerNameAsync();

        checkedCellsStartAsync();

        checkedOptionalPathAsync();

        animateMovePawnAsync();

        nextTurnAsync();

        blockViewsAsync();

        removePawnsAsync();

        showWinPlayerAsync();


        checkersViewModel.getComputerStartTurn(this)
                .observeOn(Schedulers.io())
                .filter(point -> point.getStartPoint().x != 0)
                .delay(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(move -> computerIconView.animateComputerIcon(false, move.getStartPoint()))
                .delay(350, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(move -> computerIconView.animateComputerIcon(true, move.getEndPoint()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * Show the player name winning
     */
    private void showWinPlayerAsync() {
        compositeDisposable.add(checkersViewModel
                .getWinPlayerName(this)
                .doOnNext(this::finishGame)
                .doOnNext(gameViewsManager.getTextViewTestStart()::setText)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    /**
     * Remove the relevant pawns views that need to be killed
     */
    private void removePawnsAsync() {
        compositeDisposable.add(checkersViewModel
                .removePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(gameViewsManager::removePawnView));
    }

    /**
     * Block the view if needed
     */
    private void blockViewsAsync() {
        compositeDisposable.add( checkersViewModel.isNeedBlock(this)
                .map(isNeedBlock -> !isNeedBlock)
                .doOnNext(gameViewsManager::setClickableViews)
                .subscribe());
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
                    animatePawnMove(pointsListAnimatePawn, pointPawnStartPath, 0);
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
                .doOnNext(this::checkedOptionalPathByClick)
                .doOnNext(dataCellViewClicks -> {
                    // set the pawn start in the current path
                    pawnViewStartPath = gameViewsManager.getPawn(checkersViewModel.getPointPawnByCell(dataCellViewClicks.get(0).getPoint()));
                })
                .subscribe());
    }

    /**
     * Get the relevant cells start and checked them
     * And if needed (if it is the computer turn) set the clickable to the false
     */
    private void checkedCellsStartAsync() {
        compositeDisposable.add( checkersViewModel
                .getRelevantCells(this)
                .doOnNext(this::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .doOnNext(this::checkRelevantCellsStart)
                .subscribe());
    }

    /**
     * Change the current player name
     */
    private void changePlayerNameAsync() {
        compositeDisposable.add(checkersViewModel
                .getPlayerName(this)
                .subscribe(gameViewsManager.getTextViewPlayerName()::setText));
    }

    /**
     * Checked the cells that can be start cell
     *
     * @param dataCellViewClick data for the cell start
     */
    private void checkRelevantCellsStart(DataCellViewClick dataCellViewClick){
        gameViewsManager.checkedCell(dataCellViewClick.getPoint(), dataCellViewClick.getColorChecked());
    }

    private void finishGame(String s) {



    }

    private void animatePawnMove(List<Point> pointsListAnimatePawnMove, Point pointPawnStartPath, int indexPointsListAnimatePawn) {

        Point currPoint = pointsListAnimatePawnMove.get(indexPointsListAnimatePawn);
        final int index = ++indexPointsListAnimatePawn;
        pawnViewStartPath
                .animate()
                .withLayer()
                .translationY(currPoint.y)
                .translationX(currPoint.x)
                .setDuration(250)
                .withStartAction(() -> {
                    pawnViewStartPath.setElevation(10f);
                    checkersViewModel.removePawnIfNeeded();
                })
                .withEndAction(() -> {

                    pawnViewStartPath.setElevation(0);
                    if (index < pointsListAnimatePawnMove.size()) {
                       animatePawnMove(pointsListAnimatePawnMove, pointPawnStartPath, index);
                    } else {
                        endTurn(currPoint, pointPawnStartPath);
                    }
                })
                .start();
    }

    private void onClickCell(View view) {
        checkersViewModel.getMoveOrOptionalPath(view.getX(), view.getY());
    }

    private void checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks) {

        // clear the prev cells checked
        checkedOptionalPathByClick(cellsViewOptionalPath, false);

        // checked the current cells
        checkedOptionalPathByClick(dataCellViewClicks, true);

        // Init the list to be set the prev list
        this.cellsViewOptionalPath = new ArrayList<>(dataCellViewClicks);

    }

    private void checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks, boolean isChecked){
        FluentIterable.from(dataCellViewClicks)
                .transform(dataCellViewClick -> gameViewsManager.checkedCell(dataCellViewClick.getPoint(),
                        isChecked
                                ? dataCellViewClick.getColorChecked()
                                : dataCellViewClick.getColorClearChecked()))
                .toList();
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
    private void addViewsByRelevantCells(List<DataCellViewClick> cellsViewList) {

        // add the relevant cells to the list
        FluentIterable.from(cellsViewList)
                .transform(DataCellViewClick::getPoint)
                .transform(gameViewsManager::getCellViewByPoint)
                .transform(viewsByRelevantCellsList::add)
                .toList();

    }

    /**
     * Set the end turn, clear the lists and set the relevant pawns
     *
     * @param lastPointPath of the current path that choose by the user
     * @param pointPawnStartPath of the current path that choose by the user
     */
    private void endTurn(Point lastPointPath, Point pointPawnStartPath){
        gameViewsManager.updatePawnViewStart(lastPointPath, pointPawnStartPath, pawnViewStartPath);

        // clear the optional checked path after the turn finished
        gameViewsManager.clearCheckedCellsList(cellsViewOptionalPath);
        clearPrevRelevantCellsStart();
        cellsViewOptionalPath.clear();
        checkersViewModel.finishTurn();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        compositeDisposable.dispose();
        int id= android.os.Process.myPid();
        android.os.Process.killProcess(id);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();

        int id= android.os.Process.myPid();
        android.os.Process.killProcess(id);

        finish();
    }
}