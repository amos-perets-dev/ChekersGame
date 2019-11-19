package com.example.chekersgamepro;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameBoardView = findViewById(R.id.game_board_view);

        textViewPlayerName = findViewById(R.id.text_player_name);
        textViewTestStart = findViewById(R.id.text_test_start);

        checkersViewModel = ViewModelProviders.of(MainActivity.this).get(CheckersViewModel.class);

        compositeDisposable.add(gameBoardView
                .getGameBoardView()
                .doOnNext(this::initGameBoard)
                .subscribe());

        compositeDisposable.add(initGameBoardFinish()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(Functions.actionConsumer(this::initCellsViews))
                .doOnNext(Functions.actionConsumer(this::initPawnsViews))
                .doOnNext(Functions.actionConsumer(this::drawBorders))
                .doOnError(Throwable::printStackTrace)
                .doOnNext(Functions.actionConsumer(checkersViewModel::nextTurn))
                .map(ignored -> addViewsToObservable())
                .flatMap(Observable::fromArray)
                .flatMap(Observable::fromIterable)
                .flatMap(Functions.identity())
                .doOnNext(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
//                        DataGame dataGame = DataGame.getInstance();
//                        CellDataImpl cellByPoint = dataGame.getCellByPoint(new Point((int) view.getX(), (int) view.getY()));
//                        PawnDataImpl pawnByPoint = dataGame.getInstance().getPawnByPoint(new Point((int) view.getX(), (int) view.getY()));
//                        String infoPawn = "";
//                        String infoCell = "CELL: (" + cellByPoint.getPoint().x + ", " + cellByPoint.getPoint().y +")" + "isEmpty: " + cellByPoint.isEmpty() + ", player one: " + cellByPoint.isPlayerOneCurrently() + ", leaf: " + cellByPoint.isLeaf() + "\n";
//                        if (pawnByPoint != null){
//                            infoPawn = (", PAWN: player one: " + pawnByPoint.isPlayerOne() + ", killed: " + pawnByPoint.isKilled())+ "\n";
//                        }
//                        textViewTestStart.setText(
//                                "" + infoCell + infoPawn
//                                        + ", SIZE PAWN 1: " + dataGame.getPawnsPlayerOne().size()
//                                        + ", SIZE CELL 1: " + dataGame.getCellsPlayerOne().size()+ "\n"
//                                        + ", SIZE PAWN 2: " + dataGame.getPawnsPlayerTwo().size()
//                                        + ", SIZE CELL 2: " + dataGame.getCellsPlayerTwo().size()+ "\n"
//                                        + "ALL CELL: " + dataGame.getCells().size());
                    }
                })
                .subscribe(this::onClickCell));


        compositeDisposable.add(getPlayerName()
                .subscribe(textViewPlayerName::setText));

        compositeDisposable.add( getRelevantCellsStart()
                .doOnNext(Functions.actionConsumer(this::clearPrevRelevantCells))
                .doOnNext(this::addViewsByRelevantCells)
                .flatMap(Observable::fromIterable)
                .map(cellViewMap::get)
                .map(Optional::fromNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(cellView -> cellView.checked(cellView.getColorCellCanStart()))
                .subscribe(Functions.actionConsumer(checkersViewModel::finishedCheckedRelevantCells)));

        compositeDisposable.add(checkersViewModel.getOptionalPath(this)
                .doOnNext(this::checkedOptionalPathByClick)
                .doOnNext(dataCellViewClicks -> {
                    // set the pawn start in the current path
                    if (dataCellViewClicks.size() > 0){
                        currPawnViewStartPath = pawnViewMap.get(checkersViewModel.getPointPawnByCell(dataCellViewClicks.get(0).getPoint()));
                    }
                })
                .subscribe());

        compositeDisposable.add(checkersViewModel.getMovePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pointsListAnimatePawn::addAll)
                .doOnNext(this::setCurrPointPawnViewStartPath)
                .subscribe(Functions.actionConsumer(this::animatePawnMove)));


        compositeDisposable.add(checkersViewModel
                .removePawn(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(pawnViewMap::get)
                .subscribe(PawnView::removePawn));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {


        });

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
                                pawnViewMap.remove(currPointPawnViewStartPath);
                                currPawnViewStartPath.setXY(currPoint.x, currPoint.y);
                                // now the curr point is the end point
                                pawnViewMap.put(currPoint, currPawnViewStartPath);
                                nextTurn();
                            }
                        }
                    })
                    .start();
    }

    private void clearCheckedOptionalPathViews() {

        FluentIterable.from(cellsViewOptionalPath)
                .transform(new Function<DataCellViewClick, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable DataCellViewClick dataCellViewClick) {
                        return cellViewMap.get(dataCellViewClick.getPoint()).clearChecked(dataCellViewClick.isClickValid(), dataCellViewClick.isParent());
                    }
                }).toList();
    }

    private void onClickCell(View view) {
        setClickableViews(false);
        checkersViewModel.getMoveOrOptionalPath(view.getX(), view.getY());
    }

    private void checkedOptionalPathByClick(List<DataCellViewClick> dataCellViewClicks) {

        // if the list empty need to only unblock the views
        // without cleared and checked the views
        if (dataCellViewClicks.size() > 0){
            FluentIterable.from(cellsViewOptionalPath)
                    .transform(new com.google.common.base.Function<DataCellViewClick, Object>() {
                        @Nullable
                        @Override
                        public Object apply(@Nullable DataCellViewClick dataCellViewClick) {
                            return cellViewMap.get(dataCellViewClick.getPoint())
                                    .clearChecked(dataCellViewClick.isClickValid(), dataCellViewClick.isParent());
                        }
                    }).toList();

            FluentIterable.from(dataCellViewClicks)
                    .transform(new com.google.common.base.Function<DataCellViewClick, Object>() {
                        @Nullable
                        @Override
                        public Object apply(@Nullable DataCellViewClick dataCellViewClick) {
                            return cellViewMap.get(dataCellViewClick.getPoint())
                                    .checked(dataCellViewClick.getColor());
                        }
                    }).toList();

            initViewsClicksPrev(dataCellViewClicks);
        }

        setClickableViews(true);
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
                .transform(CellView::clearChecked)
                .toList();

        viewsByRelevantCellsList.clear();
    }

    private List<Observable<? extends View>> addViewsToObservable(){
        Log.d("TEST_GAME", "private List<Observable<? extends View>> addViewsToObservable(){");

        FluentIterable.from(cellViewMap.values())
                .transform(CellView::getCell)
                .transform(viewsObservableList::add)
                .toList();

        FluentIterable.from(pawnViewMap.values())
                .transform(PawnView::getPawn)
                .transform(viewsObservableList::add)
                .toList();

        return viewsObservableList;
    }

    private void addViewsByRelevantCells(List<Point> cellsViewList) {

        // add the relevant cells to the list
        FluentIterable.from(cellsViewList)
                .transform(cellViewMap::get)
                .transform(viewsByRelevantCellsList::add)
                .toList();

    }

    private Observable<Boolean> startTurn() {
        return checkersViewModel.startTurn(MainActivity.this);
    }

    private void nextTurn(){
        clearCheckedOptionalPathViews();
        indexPointsListAnimatePawn = 0;
        pointsListAnimatePawn.clear();
        cellsViewOptionalPath.clear();
        checkersViewModel.nextTurn();
        setClickableViews(true);
    }

    private Observable<String> getPlayerName() {
        return checkersViewModel
                .getPlayerName(this);
    }

    private Observable<Boolean> initGameBoardFinish(){
        return checkersViewModel
                .initFinish(this);
    }

    private Observable<List<Point>> getRelevantCellsStart(){
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
                .setIcon(pawnData.getIcon())
                .setXY(pawnData.getStartXY().x, pawnData.getStartXY().y);

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
                .setBg(cellData.getAlphaCell())
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

    private void initGameBoard(GameBoardView gameBoardView){
        checkersViewModel.initGame(
                (int) gameBoardView.getX()
                , (int) gameBoardView.getY()
                , gameBoardView.getMeasuredWidth()
                , gameBoardView.getMeasuredHeight());
    }

    private void drawBorders(){
        gameBoardView.drawBorders(
                checkersViewModel.getBorderLines()
                , checkersViewModel.getBorderWidth()
                , checkersViewModel.getColorBorderCell());
    }

    @Override
    public void onBackPressed() {
        compositeDisposable.dispose();
        finish();
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        finish();
        super.onDestroy();

    }
}