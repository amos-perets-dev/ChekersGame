package com.example.chekersgamepro;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.data_game.DataGame;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private CheckersViewModel checkersViewModel;

    private GameBoardView gameBoardView;

    private Map<Point, CellView> cellViewMap = new HashMap<>();

    private Map<Point, PawnView> pawnViewMap = new HashMap<>();

    private List<Point> pointsListAnimatePawn = new ArrayList<>();

    private  List<Observable<? extends View>> viewsObservableList = new ArrayList<>();

    private  List<CellView> viewsByRelevantCellsList = new ArrayList<>();

    private int indexPointsListAnimatePawn = 0;


    private TextView textViewPlayerName;
    private TextView textViewTestStart;


    private int indexCellView = 0;

    private int indexPawnView = 0;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<DataCellViewClick> cellsViewOptionalPath = new ArrayList<>();

    private PawnView currPawnViewStartPath;

    private Point currPointPawnViewStartPath;

    private ImageView computerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameBoardView = findViewById(R.id.game_board_view);
        computerIcon = findViewById(R.id.computer_sign);

        textViewPlayerName = findViewById(R.id.text_player_name);
        textViewTestStart = findViewById(R.id.text_test_start);

        checkersViewModel = ViewModelProviders.of(MainActivity.this).get(CheckersViewModel.class);

        DialogChoosePlayer dialogChoosePlayer = new DialogChoosePlayer(this);

        dialogChoosePlayer.show();
        dialogChoosePlayer
                .getGameMode()
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer gameMode) throws Exception {
                        if (gameMode == DataGame.COMPUTER_GAME_MODE){

                            computerIcon
                                    .animate()
                                    .withStartAction(() -> {
                                        computerIcon.setTranslationX(gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2));
                                        computerIcon.setTranslationY(gameBoardView.getBottom() + 10);
                                    })
                                    .alpha(1)
                                    .setDuration(500)
                                    .start();
                        }
                    }
                })
                .doOnNext(this::initGameBoard)
                .subscribe();

        compositeDisposable.add(initGameBoardFinish()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(Functions.actionConsumer(this::initCellsViews))
                .doOnNext(Functions.actionConsumer(this::initPawnsViews))
                .doOnNext(Functions.actionConsumer(this::drawBorders))
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        dialogChoosePlayer.dismiss();
                    }
                })
                .doOnError(Throwable::printStackTrace)
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .map(ignored -> addViewsToObservable())
                .flatMap(Observable::fromArray)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .subscribe(this::onClickCell));



        compositeDisposable.add(getPlayerName()
                .subscribe(textViewPlayerName::setText));

        compositeDisposable.add( getRelevantCellsStart()
                .doOnNext(Functions.actionConsumer(this::clearPrevRelevantCells))
                .doOnNext(this::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .doOnNext(new Consumer<DataCellViewClick>() {
                    @Override
                    public void accept(DataCellViewClick dataCellViewClick) throws Exception {
                        CellView cellView = cellViewMap.get(dataCellViewClick.getPoint());
                        cellView.checked(dataCellViewClick.getColorChecked());
                    }
                })
                .doOnNext(new Consumer<DataCellViewClick>() {
                    @Override
                    public void accept(DataCellViewClick dataCellViewClick) throws Exception {
                        setClickableViews(checkersViewModel.isClickableViews());
                    }
                })
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

        checkersViewModel.getComputerStartTurn(this)
                .filter(list -> list.size() > 0)
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<DataCellViewClick>>() {
                    @Override
                    public void accept(List<DataCellViewClick> dataCellViewClicks) throws Exception {
                        int max = (dataCellViewClicks.size() - 1 < 0 ? 0 : dataCellViewClicks.size() - 1) + 1;
                        int index = new Random().nextInt(max);
                        Point point = dataCellViewClicks.get(index - 1 >= 0 ? index - 1 : 0).getPoint();
                        CellView cellView = cellViewMap.get(point);

                        computerIcon
                                .animate()
                                .translationY(point.y + (cellView.getMeasuredHeight() / 2))
                                .translationX(point.x)
                                .setDuration(350)
                                .withEndAction(() ->  cellView.performClick())
                                .start();
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<DataCellViewClick>>() {
                    @Override
                    public void accept(List<DataCellViewClick> dataCellViewClicks) throws Exception {
                        Point point = FluentIterable.from(checkersViewModel.getOptionalPointsListComputer())
                                .first()
                                .get();
                        CellView cellView = cellViewMap.get(point);

                        computerIcon
                                .animate()
                                .translationY(point.y + (cellView.getMeasuredHeight() / 2))
                                .translationX(point.x)
                                .setDuration(350)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        cellView.performClick();

                                        computerIcon
                                                .animate()
                                                .translationY(gameBoardView.getBottom() + 10)
                                                .translationX(gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2))
                                                .setDuration(250)
                                                .start();

                                    }
                                })
                                .start();
                    }
                })
                .subscribe();

        compositeDisposable.add(checkersViewModel
                .getWinPlayerName(this)
                .doOnNext(this::finishGame)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        textViewTestStart.setText(s);
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                }));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {


        });

    }

    private void finishGame(String s) {



    }

    private void initGameBoard(int gameMode) {
        checkersViewModel.initGame(
                (int) this.gameBoardView.getX()
                , (int) this.gameBoardView.getY()
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
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        currPawnViewStartPath.setElevation(10f);
                        checkersViewModel.removePawnIfNeeded();
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {

                        currPawnViewStartPath.setElevation(0);
                        indexPointsListAnimatePawn++;
                        if (indexPointsListAnimatePawn < pointsListAnimatePawn.size()){
                            animatePawnMove();
                        } else {
                            updatePawnViewStart(currPoint);
                            nextTurn();
                        }
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
                        .checked(DataGame.CLEAR_CHECKED))
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
                .transform(cellView -> cellView.checked(DataGame.CLEAR_CHECKED))
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
//        setClickableViews(checkersViewModel.isClickableViews());
    }

    private Observable<String> getPlayerName() {
        return checkersViewModel
                .getPlayerName(this);
    }

    private Observable<Boolean> initGameBoardFinish(){
        return checkersViewModel
                .initFinish(this);
    }

    private Observable<List<DataCellViewClick>> getRelevantCellsStart(){
        return checkersViewModel
                .getRelevantCells(this);
    }

    private void initPawnsViews() {

        FluentIterable.from(checkersViewModel.getPawns().entrySet())
                .transform(Map.Entry::getValue)
                .transform(this::createPairPawnDataAndView)
                .transform(this::initPawnView)
                .transform(this::addPawnToMapView)
                .toList();

    }

    private Pair<PawnDataImpl, PawnView> createPairPawnDataAndView(PawnDataImpl pawnData){
        int id = getResources().getIdentifier("pawn" + (indexPawnView + 1), "id", getPackageName());
        indexPawnView++;

        return new Pair<>(pawnData, findViewById(id));
    }

    private Pair<Point, PawnView> initPawnView(Pair<PawnDataImpl, PawnView> input){
        PawnDataImpl pawnData = input.first;

        PawnView pawnView = input.second;
//        pawnView.setVisibility(View.GONE);
        pawnView
                .setWidth(pawnData.getWidth())
                .setHeight(pawnData.getHeight())
                .setRegularIcon(pawnData.getRegularIcon())
                .setQueenIcon(pawnData.getQueenIcon())
                .setXY(pawnData.getStartXY().x, pawnData.getStartXY().y)
                .setIsReady(true);

        return new Pair<>(pawnData.getStartXY(), pawnView);
    }

    private void initCellsViews() {
        Set<Map.Entry<Point, CellDataImpl>> entries = checkersViewModel.getCells().entrySet();
        FluentIterable.from(entries)
                .transform(Map.Entry::getValue)
                .transform(this::createPairCellDataAndView)
                .transform(this::initCellView)
                .transform(this::addCellToMapView)
                .toList();
    }

    private Pair<CellDataImpl, CellView> createPairCellDataAndView(CellDataImpl cellData){
        int id = getResources().getIdentifier("cell" + (indexCellView + 1), "id", getPackageName());
        indexCellView++;

        return new Pair<>(cellData, findViewById(id));
    }

    private Pair<Point, CellView> initCellView(Pair<CellDataImpl, CellView> input){
        CellDataImpl cellData = input.first;
        CellView cellView = input.second
                .setWidth(cellData.getWidth())
                .setHeight(cellData.getHeight())
                .setBg(cellData.getAlphaCell(), cellData.isMasterCell() && cellData.isValidCell())
                .setXY(cellData.getPoint().x, (cellData.getPoint().y));

        return new Pair<>(cellData.getPoint(), cellView);

    }

    private boolean addCellToMapView(Pair<Point, CellView> input){
        Point point = input.first;
        CellView cellView = input.second;

        cellViewMap.put(point, cellView);
        return true;
    }

    private boolean addPawnToMapView(Pair<Point, PawnView> input){

        pawnViewMap.put(input.first, input.second);
        return true;

    }


    private <T extends View> T addToMapView(Pair<Point, T> input){

        Map<Point, T> map = (Map<Point, T>) pawnViewMap;

        if (input.second instanceof CellView){
            map = (Map<Point, T>) cellViewMap;
        }

        return map.put(input.first, input.second);
    }

    private void drawBorders(){
        gameBoardView.drawBorders(
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