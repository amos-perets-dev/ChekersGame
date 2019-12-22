package com.example.chekersgamepro;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.move.Move;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private CheckersViewModel checkersViewModel;

    private Map<Point, CellView> cellViewMap = new HashMap<>();

    private Map<Point, PawnView> pawnViewMap = new HashMap<>();

    private List<Point> pointsListAnimatePawn = new ArrayList<>();

    private  List<Observable<? extends View>> viewsObservableList = new ArrayList<>();

    private  List<CellView> viewsByRelevantCellsList = new ArrayList<>();

    private int indexPointsListAnimatePawn = 0;

    private int indexCellView = 0;

    private int indexPawnView = 0;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<DataCellViewClick> cellsViewOptionalPath = new ArrayList<>();

    private PawnView currPawnViewStartPath;

    private Point currPointPawnViewStartPath;

    private GameViewsManager gameViewsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkersViewModel = ViewModelProviders.of(MainActivity.this).get(CheckersViewModel.class);

        gameViewsManager = new GameViewsManager(this, checkersViewModel);

        DialogGameMode dialogGameMode = new DialogGameMode(this);

        // Open dialog to choose game mode
        // init the game board, pawns and cells
        // create observables to the views
        compositeDisposable.add(dialogGameMode
                .getGameMode()
                .doOnNext(this::initViews)
                .doOnNext(Functions.actionConsumer(dialogGameMode::dismiss))
                .doOnError(Throwable::printStackTrace)
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .map(ignored -> addViewsToObservable())
                .flatMap(Observable::fromArray)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .doOnNext(view -> gameViewsManager.setTest(view))
                .subscribe(this::onClickCell));

        compositeDisposable.add(checkersViewModel
                .getPlayerName(this)
                .subscribe(gameViewsManager.getTextViewPlayerName()::setText));

        // Get the relevant cells start and checked them
        compositeDisposable.add( checkersViewModel
                .getRelevantCells(this)
                .doOnNext(Functions.actionConsumer(this::clearPrevRelevantCells))
                .doOnNext(this::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .doOnNext(this::checkRelevantCellsStart)
                .map(ignored -> checkersViewModel.isClickableViews())
                .doOnNext(this::setClickableViews)
                .subscribe());

        // checked the optional path by click
        compositeDisposable.add(checkersViewModel
                .getOptionalPath(this)
                .doOnNext(this::checkedOptionalPathByClick)
                .doOnNext(dataCellViewClicks -> {
                    // set the pawn start in the current path
                    if (dataCellViewClicks.size() > 0){
                        currPawnViewStartPath = pawnViewMap.get(checkersViewModel.getPointPawnByCell(dataCellViewClicks.get(0).getPoint()));
                    }
                })
                .subscribe());

        // animate the move of the relevant pawn
        compositeDisposable.add(checkersViewModel
                .getMovePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pointsListAnimatePawn::addAll)
                .doOnNext(this::setCurrPointPawnViewStartPath)
                .subscribe(Functions.actionConsumer(this::animatePawnMove)));

        // remove the relevant pawns that need to be killed
        compositeDisposable.add(checkersViewModel
                .removePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(pawnViewMap::get)
                .subscribe(PawnView::removePawn));

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


        checkersViewModel.getComputerStartTurn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(point -> point.getStartPoint().x != 0)
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Move>() {
                    @Override
                    public void accept(Move move) throws Exception {

                        Point point = move.getStartPoint();
                        CellView cellView = cellViewMap.get(point);

                        gameViewsManager
                                .getComputerIcon()
                                .animate()
                                .translationY(point.y + (cellView.getMeasuredHeight() / 2))
                                .translationX(point.x)
                                .setDuration(300)
                                .withEndAction(() ->  cellView.performClick())
                                .start();
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Move>() {
                    @Override
                    public void accept(Move move) throws Exception {
                        GameBoardView gameBoardView = gameViewsManager
                                .getGameBoardView();
                        ImageView computerIcon = gameViewsManager.getComputerIcon();
                        Point point = move.getEndPoint();
                        CellView cellView = cellViewMap.get(point);

                        gameViewsManager
                                .getComputerIcon()
                                .animate()
                                .translationY(point.y + (cellView.getMeasuredHeight() / 2))
                                .translationX(point.x)
                                .setDuration(300)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        cellView.performClick();

                                        computerIcon
                                                .animate()
                                                .translationY(gameBoardView.getBottom() + 10)
                                                .translationX(gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2))
                                                .setDuration(250)
                                                .start();

                                    }
                                })
                                .start();
                    }
                })

                .subscribe();



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {


        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

    }

    private void initViews(Integer gameMode) {

         gameViewsManager.initComputerIcon(gameMode);
         initGameBoard(gameMode);
         cellViewMap.putAll(gameViewsManager.initCellsViews());
         pawnViewMap.putAll(gameViewsManager.initPawnsViews());
         drawBorders();
    }

    /**
     * Checked the cells that can be start cell
     * @param dataCellViewClick
     */
    private void checkRelevantCellsStart(DataCellViewClick dataCellViewClick){
        CellView cellView = cellViewMap.get(dataCellViewClick.getPoint());
        cellView.checked(dataCellViewClick.getColorChecked());
    }

    private void finishGame(String s) {



    }

    private void initGameBoard(int gameMode) {
        GameBoardView gameBoardView = gameViewsManager.getGameBoardView();
        checkersViewModel.initGame(
                (int) gameBoardView.getX()
                , (int)gameBoardView.getY()
                , gameBoardView.getMeasuredWidth()
                , gameBoardView.getMeasuredHeight()
                , gameMode);
    }

    /**
     * Set the point of the start pawn in the current path
     *
     * @param currentPointList of the path
     */
    public void setCurrPointPawnViewStartPath(List<Point> currentPointList) {
        this.currPointPawnViewStartPath = currentPointList.get(0);
    }

    private void setClickableViews(boolean isClickable){
        FluentIterable.from(pawnViewMap.values())
                .transform(pawnView -> pawnView.setEnabledPawn(isClickable))
                .toList();

        FluentIterable.from(cellViewMap.values())
                .transform(cellView -> cellView.setEnabledCell(isClickable))
                .toList();
    }

    private void animatePawnMove() {

        Point currPoint = pointsListAnimatePawn.get(indexPointsListAnimatePawn);

        currPawnViewStartPath
                .animate()
                .withLayer()
                .translationY(currPoint.y)
                .translationX(currPoint.x)
                .setDuration(250)
                .withStartAction(() -> {
                    currPawnViewStartPath.setElevation(10f);
                    checkersViewModel.removePawnIfNeeded();
                })
                .withEndAction(() -> {

                    currPawnViewStartPath.setElevation(0);
                    indexPointsListAnimatePawn++;
                    if (indexPointsListAnimatePawn < pointsListAnimatePawn.size()){
                        animatePawnMove();
                    } else {
                        updatePawnViewStart(currPoint);
                        nextTurn();
                    }
                })
                .start();
    }

    /**
     * Update the pawn view point when the animate move finished
     *
     * @param point the path end point
     */
    private void updatePawnViewStart(Point point){
        pawnViewMap.remove(currPointPawnViewStartPath);
        currPawnViewStartPath.setXY(point.x, point.y);
        // now the curr point is the end point
        pawnViewMap.put(point, currPawnViewStartPath);
        currPawnViewStartPath.setIcon(checkersViewModel.isQueenPawn(point));
    }

    private void clearCheckedOptionalPathViewsAfterEndTurn() {
        FluentIterable.from(cellsViewOptionalPath)
                .transform(dataCellViewClick -> cellViewMap.get(dataCellViewClick.getPoint())
                        .checked(DataGame.ColorCell.CLEAR_CHECKED))
                .toList();
    }

    private void onClickCell(View view) {
        setClickableViews(false);
        checkersViewModel.getMoveOrOptionalPath(view.getX(), view.getY());
    }

    private void checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks) {

        // if the list empty need to only unblock the views
        // without cleared and checked the views
        if (dataCellViewClicks.size() > 0){
            // clear the prev cells checked
            FluentIterable.from(cellsViewOptionalPath)
                    .transform(dataCellViewClick -> checkCellView(dataCellViewClick.getPoint(), dataCellViewClick.getColorClearChecked()))
                    .toList();

            // checked the current cells
            FluentIterable.from(dataCellViewClicks)
                    .transform(dataCellViewClick -> checkCellView(dataCellViewClick.getPoint(), dataCellViewClick.getColorChecked()))
                    .toList();

            initViewsClicksPrev(dataCellViewClicks);
        }

        setClickableViews(checkersViewModel.isClickableViews());
    }

    private CellView checkCellView(Point point, int color) {
        return cellViewMap.get(point)
                .checked(color);
    }

    /**
     * Init the curr list to the prev list
     * @param dataCellViewClicks
     */
    private void initViewsClicksPrev(List<DataCellViewClick> dataCellViewClicks){
        this.cellsViewOptionalPath.clear();
        this.cellsViewOptionalPath.addAll(dataCellViewClicks);
    }

    /**
     * Clear the views list of the prev relevant cells
     */
    private void clearPrevRelevantCells() {

        FluentIterable.from(viewsByRelevantCellsList)
                .transform(cellView -> cellView.checked(DataGame.ColorCell.CLEAR_CHECKED))
                .toList();

        viewsByRelevantCellsList.clear();
    }

    private List<Observable<? extends View>> addViewsToObservable(){
        FluentIterable.from(cellViewMap.values())
                .transform(CellView::getCellClick)
                .transform(viewsObservableList::add)
                .toList();

        FluentIterable.from(pawnViewMap.values())
                .transform(PawnView::getPawnClick)
                .transform(viewsObservableList::add)
                .toList();

        return viewsObservableList;
    }

    private void addViewsByRelevantCells(List<DataCellViewClick> cellsViewList) {

        // add the relevant cells to the list
        FluentIterable.from(cellsViewList)
                .transform(DataCellViewClick::getPoint)
                .transform(cellViewMap::get)
                .transform(viewsByRelevantCellsList::add)
                .toList();

    }

    private void nextTurn(){
        clearCheckedOptionalPathViewsAfterEndTurn();
        indexPointsListAnimatePawn = 0;
        pointsListAnimatePawn.clear();
        cellsViewOptionalPath.clear();
        checkersViewModel.nextTurn();
    }

    private void drawBorders(){
        gameViewsManager.getGameBoardView().drawBorders(
                checkersViewModel.getBorderLines()
                , checkersViewModel.getBorderWidth()
                , checkersViewModel.getColorBorderCell());
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