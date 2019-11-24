package com.example.chekersgamepro.data_game;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.GameManager;
import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class DataGame implements GameManager.ChangePlayerListener {

    public static final int CHECKED_PAWN_START_END = Color.argb(155, 45, 170, 0);
    public static final int INVALID_CHECKED = Color.argb(155, 170, 0, 0);
    public static final int CAN_CELL_START = Color.argb(155, 243, 215, 0);
    public static final int INSIDE_PATH = Color.argb(155, 0, 43, 170);
    public static final int CLEAR_CHECKED = R.drawable.cell_1;

    public static final int LEFT_TOP_DIRECTION = 1;
    public static final int LEFT_BOTTOM_DIRECTION = 2;
    public static final int RIGHT_TOP_DIRECTION = 3;
    public static final int RIGHT_BOTTOM_DIRECTION = 4;

    private static DataGame instance = null;
    public static final int RIGHT = 1;
    public static final int LEFT = -1;

    private final int GAME_BOARD_SIZE = 8;
    private final int DIV_SIZE_CELL = 14;
    private final int COLOR_BORDER_CELL = Color.BLACK;
    private final int BORDER_WIDTH = 2;

    private List<BorderLine> borderLines = new ArrayList<>();

    private Map<Point, CellDataImpl> cells = new HashMap<>();
    private Map<Point, CellDataImpl> cellsPlayerOne = new HashMap<>();
    private Map<Point, CellDataImpl> cellsPlayerTwo = new HashMap<>();

    private Map<Point, PawnDataImpl> pawns = new HashMap<>();
    private Map<Point, PawnDataImpl> pawnsPlayerOne  = new HashMap<>();
    private Map<Point, PawnDataImpl> pawnsPlayerTwo = new HashMap<>();

    private boolean isPlayerOneTurn;

    private DataGameHelper dataGameHelper = new DataGameHelper();

    private DataGame() {
        // Exists only to defeat instantiation.
    }

    synchronized public static DataGame getInstance() {
        if(instance == null) {
            instance = new DataGame();
        }
        return instance;
    }

    public void addChanePlayerListener(List<GameManager.ChangePlayerListener> changePlayerListListeners){
        changePlayerListListeners.add(this);
    }

    public Map<Point, CellDataImpl> getCellsPlayerOne() {
        return cellsPlayerOne;
    }

    public Map<Point, CellDataImpl> getCellsPlayerTwo() {
        return cellsPlayerTwo;
    }

    public Map<Point, CellDataImpl> getCells() {
        return cells;
    }

    public Map<Point, PawnDataImpl> getPawns() {
        return pawns;
    }

    public Map<Point, PawnDataImpl> getPawnsPlayerOne() {
        return pawnsPlayerOne;
    }

    public Map<Point, PawnDataImpl> getPawnsPlayerTwo() {
        return pawnsPlayerTwo;
    }

    public void removePawnByPlayer(PawnDataImpl pawnData) {
        if(pawnData.isPlayerOne()){
            pawnsPlayerOne.remove(pawnData.getStartXY());
        } else{
            pawnsPlayerTwo.remove(pawnData.getStartXY());
        }
        pawns.remove(pawnData.getStartXY());
    }

    public void putPawnByPlayer(PawnDataImpl pawnData) {
        if (pawnData.isPlayerOne()) {
            pawnsPlayerOne.put(pawnData.getStartXY(), pawnData);
        } else {
            pawnsPlayerTwo.put(pawnData.getStartXY(), pawnData);
        }
        pawns.put(pawnData.getStartXY(), pawnData);
    }

    public void putCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.isPlayerOneCurrently()&& !cellDataImpl.isEmpty()) {
            cellsPlayerOne.put(cellDataImpl.getPoint(), cellDataImpl);
        } else if (!cellDataImpl.isPlayerOneCurrently() && !cellDataImpl.isEmpty()) {
            cellsPlayerTwo.put(cellDataImpl.getPoint(), cellDataImpl);
        }
        cells.put(cellDataImpl.getPoint(), cellDataImpl);
    }

    public void removeCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.isPlayerOneCurrently()) {
            cellsPlayerOne.remove(cellDataImpl.getPoint());
        } else if (!cellDataImpl.isPlayerOneCurrently() ) {
            cellsPlayerTwo.remove(cellDataImpl.getPoint());
        }
    }

    /**
     * Update the new pawn
     * Remove the old pawn (that mean the old point)
     * Update the new pawn (that mean the new point)
     *
     * @param pawnData need to remove and after update it need to insert again
     * @param cellDataDst the new points
     */
    public void updatePawn(PawnDataImpl pawnData, CellDataImpl cellDataDst) {
        removePawnByPlayer(pawnData);
        pawnData.setContainerCellXY(cellDataDst.getPoint());
        pawnData.setStartXY(cellDataDst.getPointStartPawn());
        pawnData.setMasterPawn(pawnData.isMasterPawn() || cellDataDst.isMasterCell());
        putPawnByPlayer(pawnData);
    }

    /**
     * Update the new cell
     *
     * @param cellData that need to be set
     * @param isEmpty is the current cell is empty or not
     * @param isPlayerOneCurrently the player one currently
     */
    public void updateCell(CellDataImpl cellData, boolean isEmpty, boolean isPlayerOneCurrently){
        // firstable remove the cell because after it the cell change the data/value
        cellData.setEmpty(isEmpty);
        cellData.setPlayerOneCurrently(isPlayerOneCurrently);
        cells.put(cellData.getPoint(), cellData);
    }

    public PawnDataImpl getPawnByPoint(Point point) {
        return pawns.get(point);
    }

    public CellDataImpl getCellByPoint(Point point) {
        CellDataImpl cellData = cells.get(point);
        PawnDataImpl pawnData = getPawnByPoint(point);

        return cellData != null
                ? cellData
                : pawnData != null ? cells.get(pawnData.getContainerCellXY()) : null;
    }

    public void updatePawnKilled(PawnDataImpl pawnData) {
        CellDataImpl cellByPoint = getCellByPoint(pawnData.getContainerCellXY());

        // update that the cell is change to empty because the pawn killed
        updateCell(cellByPoint, true, false);
        pawnData.setKilled(true);
        removePawnByPlayer(pawnData);
        removeCellByPlayer(cellByPoint);
    }

    @Override
    public void onChangePlayer(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn;
    }

    public boolean isPlayerOneTurn() {
        return isPlayerOneTurn;
    }

    public CellDataImpl getNextCell(CellDataImpl cellData, boolean isLeft) {
        return dataGameHelper.getNextCell(cellData, isLeft, isPlayerOneTurn);
    }

    public CellDataImpl getNextCell(CellDataImpl currCellData, CellDataImpl prevCellData) {
        return dataGameHelper.getNextCell(currCellData, prevCellData);
    }

    public CellDataImpl getNextCellByDirection(CellDataImpl currCellData, int direction) {
        return dataGameHelper.getNextCellByDirection(currCellData, direction);
    }

}