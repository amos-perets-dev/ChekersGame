package com.example.chekersgamepro;

import android.app.Activity;
import android.graphics.Point;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.graphic.cell.CellView;
import com.example.chekersgamepro.graphic.game_board.GameBoardView;
import com.example.chekersgamepro.graphic.pawn.PawnView;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameViewsManager {

    private TextView textViewPlayerName;
    private TextView textViewTestStart;

    private ImageView computerIcon;

    private GameBoardView gameBoardView;

    private final Activity activity;

    private final CheckersViewModel checkersViewModel;

    private int indexCellView = 0;

    private int indexPawnView = 0;

    public GameViewsManager(Activity activity, CheckersViewModel checkersViewModel) {
        this.activity = activity;
        this.checkersViewModel = checkersViewModel;
        findViews(activity);

    }

    private void findViews(Activity activity) {

        gameBoardView = activity.findViewById(R.id.game_board_view);
        computerIcon = activity.findViewById(R.id.computer_sign);

        textViewPlayerName = activity.findViewById(R.id.text_player_name);
        textViewTestStart = activity.findViewById(R.id.text_test_start);

    }

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }

    public void initComputerIcon(Integer gameMode) {

        if (gameMode == DataGame.Mode.COMPUTER_GAME_MODE){
            computerIcon
                    .animate()
                    .withStartAction(() -> {
                        // set the icon computer location on the screen
                        computerIcon.setTranslationX(gameBoardView.getX() + (gameBoardView.getMeasuredWidth() / 2) - (computerIcon.getMeasuredWidth() / 2));
                        computerIcon.setTranslationY(gameBoardView.getBottom() + 10);
                    })
                    .alpha(1)
                    .setDuration(500)
                    .start();
        }
    }

    public Map<Point, CellView> initCellsViews() {

        Map<Point, CellView> cellViewMap = new HashMap<>();

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

    private Pair<CellDataImpl, CellView> createPairCellDataAndView(CellDataImpl cellData){
        int id = activity.getResources().getIdentifier("cell" + (indexCellView + 1), "id", activity.getPackageName());
        indexCellView++;

        return new Pair<>(cellData, activity.findViewById(id));
    }

    private Pair<Point, CellView> initCellView(Pair<CellDataImpl, CellView> input){
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
        Map<Point, PawnView> pawnViewMap = new HashMap<>();
        FluentIterable.from(checkersViewModel.getPawns().entrySet())
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

    private Pair<PawnDataImpl, PawnView> createPairPawnDataAndView(PawnDataImpl pawnData){
        int id = activity.getResources().getIdentifier("pawn" + (indexPawnView + 1), "id", activity.getPackageName());
        indexPawnView++;

        return new Pair<>(pawnData, activity.findViewById(id));
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

    public void setTest(View view) {

        DataGame dataGame = DataGame.getInstance();
        CellDataImpl cellByPoint = dataGame.getCellByPoint(new Point((int) view.getX(), (int) view.getY()));
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(new Point((int) view.getX(), (int) view.getY()));
        String infoPawn = "";
        String infoCell = cellByPoint.toString() + "\n";
        if (pawnByPoint != null){
            infoPawn = pawnByPoint.toString() + "\n";
        }
        textViewTestStart.setText(
                "" + infoCell + infoPawn
                        + ", SIZE PAWN 1: " + dataGame.getPawnsPlayerOne().size()
                        + ", SIZE CELL 1: " + dataGame.getCellsPlayerOne().size()+ "\n"
                        + ", SIZE PAWN 2: " + dataGame.getPawnsPlayerTwo().size()
                        + ", SIZE CELL 2: " + dataGame.getCellsPlayerTwo().size()+ "\n"
                        + "ALL CELL: " + dataGame.getCells().size());

    }

    public TextView getTextViewPlayerName() {
        return textViewPlayerName;
    }

    public TextView getTextViewTestStart() {
        return textViewTestStart;
    }

    public ImageView getComputerIcon() {
        return computerIcon;
    }
}
