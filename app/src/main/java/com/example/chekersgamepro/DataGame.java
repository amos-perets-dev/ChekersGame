package com.example.chekersgamepro;

import android.graphics.Color;
import android.graphics.Point;

import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGame {

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

    private DataGame() {
        // Exists only to defeat instantiation.
    }

    synchronized public static DataGame getInstance() {
        if(instance == null) {
            instance = new DataGame();
        }
        return instance;
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
        if(pawnData.isPlayerOneCurrently()){
            pawnsPlayerOne.remove(pawnData.getStartXY());
        } else{
            pawnsPlayerTwo.remove(pawnData.getStartXY());
        }
        pawns.remove(pawnData.getStartXY());
    }

    public void putPawnByPlayer(PawnDataImpl pawnData) {
        if (pawnData.isPlayerOneCurrently()) {
            pawnsPlayerOne.put(pawnData.getStartXY(), pawnData);
        } else {
            pawnsPlayerTwo.put(pawnData.getStartXY(), pawnData);
        }
        pawns.put(pawnData.getStartXY(), pawnData);
    }

    public void putCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.isPlayerOneCurrently()) {
            cellsPlayerOne.put(cellDataImpl.getPoint(), cellDataImpl);
        } else if (!cellDataImpl.isEmpty()) {
            cellsPlayerTwo.put(cellDataImpl.getPoint(), cellDataImpl);
        }
        cells.put(cellDataImpl.getPoint(), cellDataImpl);
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
        cellData.setEmpty(isEmpty);
        cellData.setPlayerOneCurrently(isPlayerOneCurrently);
        putCellByPlayer(cellData);
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

    public DataGame updatePawnKilled(PawnDataImpl pawnData) {

        // update that the cell is change to empty because the pawn killed
        updateCell(getCellByPoint(pawnData.getContainerCellXY()), true, false);
        pawnData.setKilled(true);
        removePawnByPlayer(pawnData);
        return this;
    }
}
