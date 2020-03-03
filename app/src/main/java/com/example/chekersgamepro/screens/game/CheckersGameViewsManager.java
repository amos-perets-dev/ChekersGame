package com.example.chekersgamepro.screens.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.example.chekersgamepro.screens.game.model.GameFinishData;
import com.example.chekersgamepro.util.CheckersApplication;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CheckersGameViewsManager {

    private TextView textViewTestStart;


    private GameBoardView gameBoardView;

    private final Activity activity;

    private final CheckersViewModel checkersViewModel;

    private int indexCellView = 0;

    private int indexPawnView = 0;

    private Map<Point, CellView> cellViewMap = new HashMap<>();

    private Map<Point, PawnView> pawnViewMap = new HashMap<>();

    private List<Observable<? extends View>> viewsObservableList = new ArrayList<>();

    private ComputerIconView computerIconView;

    private View gameBoardContainer;

    private ProgressBar progressBarTop;
    private ProgressBar progressBarBottom;

    private PlayersNamesView playersNamesView;

    private int progress = 0;
    private Disposable disposableTimer;

    public CheckersGameViewsManager(Activity activity, CheckersViewModel checkersViewModel, CompositeDisposable compositeDisposable) {
        this.activity = activity;
        this.checkersViewModel = checkersViewModel;
        findViews(activity);

        computerIconView = new ComputerIconView(activity, gameBoardView);

        playersNamesView = new PlayersNamesView(activity);

        compositeDisposable.add(
                checkersViewModel.getTotalPawnsChanges()
                        .subscribe(playersNamesView::setData)
        );

    }

    private void findViews(Activity activity) {

        gameBoardView = activity.findViewById(R.id.game_board_view);
        gameBoardContainer = activity.findViewById(R.id.container_game_board);

        textViewTestStart = activity.findViewById(R.id.text_test_start);

        progressBarTop = activity.findViewById(R.id.progress_top);
        progressBarBottom = activity.findViewById(R.id.progress_bottom);

    }

    public void notifyProgress() {
        if (disposableTimer != null) disposableTimer.dispose();

        ProgressBar progressBar;
        if (checkersViewModel.isTopPlayer()) {
            progressBar = progressBarTop;
        } else {
            progressBar = progressBarBottom;
        }

        disposableTimer = Observable.interval(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(x -> {
                    progressBar.setProgress(progress);
                    progress++;
                })
                .takeUntil(x -> progress == 3000)
                .filter(ignored -> progress == 3000)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        CheckersApplication.create().showToast("TIME OVER");
                        checkersViewModel.notifyTimeOver();
                    }
                });

    }

    public Completable resetProgress() {
        progressBarTop.setProgress(0);
        progressBarBottom.setProgress(0);
        progress = 0;
        if (disposableTimer != null) disposableTimer.dispose();
        return Completable.complete();
    }

    public Map<Point, CellView> initCellsViews() {

        cellViewMap = new HashMap<>();

        FluentIterable.from(checkersViewModel.getCells().entrySet())
                .transform(Map.Entry::getValue)
                .transform(this::createPairCellDataAndView)
                .transform(this::initCellView)
                .transform(new Function<Pair<Point, CellView>, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable Pair<Point, CellView> input) {
                        cellViewMap.put(input.first, input.second);
                        return "";
                    }
                })
                .toList();

        return cellViewMap;
    }

    private Pair<CellDataImpl, CellView> createPairCellDataAndView(CellDataImpl cellData) {
        int id = activity.getResources().getIdentifier("cell" + (indexCellView + 1), "id", activity.getPackageName());
        indexCellView++;

        return new Pair<>(cellData, activity.findViewById(id));
    }

    private Pair<Point, CellView> initCellView(Pair<CellDataImpl, CellView> input) {
        CellDataImpl cellData = input.first;
        CellView cellView = input.second
                .setWidth(cellData.getWidthCell())
                .setHeight(cellData.getHeightCell())
                .setBg(cellData.getAlphaCell(), cellData.isMasterCell())
                .setXY(cellData.getPointCell().x, (cellData.getPointCell().y))
                .setIsCanClick(cellData.getCellContain() != DataGame.CellState.EMPTY_INVALID);

        return new Pair<>(cellData.getPointCell(), cellView);

    }

    public Map<Point, PawnView> initPawnsViews() {
        pawnViewMap = new HashMap<>();
        Set<Map.Entry<Point, PawnDataImpl>> entrySet = checkersViewModel.getPawns().entrySet();
        Log.d("TEST_GAME", "entrySet: " + entrySet.size());

        FluentIterable.from(entrySet)
                .transform(Map.Entry::getValue)
                .transform(this::createPairPawnDataAndView)
                .transform(this::initPawnView)
                .transform(new Function<Pair<Point, PawnView>, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable Pair<Point, PawnView> input) {
                        pawnViewMap.put(input.first, input.second);
                        return "";
                    }
                })
                .toList();

        return pawnViewMap;
    }

    private Pair<PawnDataImpl, PawnView> createPairPawnDataAndView(PawnDataImpl pawnData) {
        int id = activity.getResources().getIdentifier("pawn" + (indexPawnView + 1), "id", activity.getPackageName());
        indexPawnView++;

        return new Pair<>(pawnData, activity.findViewById(id));
    }

    private Pair<Point, PawnView> initPawnView(Pair<PawnDataImpl, PawnView> input) {
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

    public void setTest(View view) {

        DataGame dataGame = DataGame.getInstance();
        CellDataImpl cellByPoint = dataGame.getCellByPoint(new Point((int) view.getX(), (int) view.getY()));
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(new Point((int) view.getX(), (int) view.getY()));
        String infoPawn = "";
        String infoCell = cellByPoint.toString() + "\n";
        if (pawnByPoint != null) {
            infoPawn = pawnByPoint.toString() + "\n";
        }


        String text = "" + infoCell + infoPawn
                + ", SIZE PAWN 1: " + dataGame.getPawnsPlayerOne().size()
                + ", SIZE CELL 1: " + dataGame.getCellsPlayerOne().size() + "\n"
                + ", SIZE PAWN 2: " + dataGame.getPawnsPlayerTwo().size()
                + ", SIZE CELL 2: " + dataGame.getCellsPlayerTwo().size() + "\n"
                + "ALL CELL: " + dataGame.getCells().size();
//        textViewTestStart.setText(
//                text);

//        Log.d("TEST_GAME", "TEST: " + text);

    }

    public void setPlayerTurn(boolean isPlayerOneTurn) {
        playersNamesView.setPlayerTurn(isPlayerOneTurn);
    }

    public TextView getTextViewTestStart() {
        return textViewTestStart;
    }

    public Map<Point, CellView> getCellViewMap() {
        return cellViewMap;
    }

    public Map<Point, PawnView> getPawnViewMap() {
        return pawnViewMap;
    }

    public List<Observable<? extends View>> addViewsToObservable(int ignored) {
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

    /**
     * Update the pawn view by point when the animate move finished
     *
     * @param point the path end point
     */
    public void updatePawnViewStart(Point point, Point currPointPawnViewStartPath, PawnView currPawnViewStartPath) {
        pawnViewMap.remove(currPointPawnViewStartPath);
        currPawnViewStartPath.setXY(point.x, point.y);
        // now the curr point is the end point
        pawnViewMap.put(point, currPawnViewStartPath);
        currPawnViewStartPath.setIcon(checkersViewModel.isQueenPawn(point));
    }

    public PawnView getPawn(Point pointPawnByCell) {
        return pawnViewMap.get(pointPawnByCell);
    }

    public void removePawnView(Point point) {
        pawnViewMap.get(point).removePawn();
    }

    public CellView getCellViewByPoint(Point point) {
        if (cellViewMap == null) return null;
        return cellViewMap.get(point);
    }

    public void clearCheckedCell(Point point) {
         checkedCell(point, DataGame.ColorCell.CLEAR_CHECKED);
    }

    public void checkedCell(Point point, int color) {
        CellView cellView = cellViewMap.get(point);
        if (cellView != null) {
             cellView.checked(color);
        }
    }

    public void initViews(Intent intent) {

        int gameMode = intent.getIntExtra("GAME_MODE", DataGame.Mode.COMPUTER_GAME_MODE);
        String playerOne = Strings.nullToEmpty(intent.getStringExtra("PLAYER_ONE"));
        String playerTwo = Strings.nullToEmpty(intent.getStringExtra("PLAYER_TWO"));

        initGameBoard(gameMode, playerOne, playerTwo);
        initCellsViews();
        initPawnsViews();
        gameBoardView.drawBorders(
                checkersViewModel.getBorderLines()
                , checkersViewModel.getBorderWidth()
                , checkersViewModel.getColorBorderCell());
        computerIconView.initComputerIcon(gameMode, progressBarTop);

    }

    private void initGameBoard(int gameMode, String playerOne, String playerTwo) {

        checkersViewModel.initGame(
                (int) gameBoardView.getX()
                , (int) gameBoardView.getY()
                , gameBoardView.getMeasuredWidth()
                , gameBoardView.getMeasuredHeight()
                , gameMode
                , playerOne
                , playerTwo);
    }

    public void clearCheckedCellsList(List<DataCellViewClick> cellsViewOptionalPath) {
        for (DataCellViewClick cellViewClick : cellsViewOptionalPath) {
            clearCheckedCell(cellViewClick.getPoint());
        }
    }

    public Completable animateComputerIconView(Pair<Point, Boolean> pair) {
        if (pair == null) return Completable.error(new Throwable("pair == null"));

        Point pointCell = pair.first;
        return computerIconView.animateComputerIcon(pair.second, pointCell, getCellViewByPoint(pointCell))
                .doOnEvent(throwable -> checkersViewModel.setMoveOrOptionalPath(pair.first));
    }

    public void showViews() {
        gameBoardContainer
                .animate()
                .withLayer()
                .alpha(1)
                .setDuration(800)
                .start();

        playersNamesView.showViewsWithAnimate();

    }

    public void setFinishGame(GameFinishData finishGame) {
        disposableTimer.dispose();
        String winOrLoose = finishGame.getWinOrLoose();
        textViewTestStart.setText(winOrLoose);
        CheckersApplication.create().showToast(winOrLoose);
    }
}
