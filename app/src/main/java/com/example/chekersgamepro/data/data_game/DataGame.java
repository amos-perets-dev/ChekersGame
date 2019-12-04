package com.example.chekersgamepro.data.data_game;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.GameManager;
import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGame extends DataGameHelper implements GameManager.ChangePlayerListener {

    public static final int GAME_BOARD_SIZE = 8;

    private static DataGame instance = null;

    private final int DIV_SIZE_CELL = 14;
    private final int COLOR_BORDER_CELL = Color.BLACK;
    private final int BORDER_WIDTH = 2;
    private int gameMode = -1;

    private Map<Point, CellDataImpl> cells = new HashMap<>();
    private Map<Point, CellDataImpl> cellsPlayerOne = new HashMap<>();
    private Map<Point, CellDataImpl> cellsPlayerTwo = new HashMap<>();

    private Map<Point, PawnDataImpl> pawns = new HashMap<>();
    private Map<Point, PawnDataImpl> pawnsPlayerOne  = new HashMap<>();
    private Map<Point, PawnDataImpl> pawnsPlayerTwo = new HashMap<>();

    private boolean isPlayerOneTurn;

    private DataGameHelper dataGameHelper = new DataGameHelper();

    private int countKing = 0;

    private CellDataImpl[][] boardCells = new CellDataImpl[GAME_BOARD_SIZE][GAME_BOARD_SIZE];

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

    public DataGame setCells(Map<Point, CellDataImpl> cells) {
        this.cells = new HashMap<>(cells);
        return this;
    }

    public DataGame setCellsPlayerOne(Map<Point, CellDataImpl> cellsPlayerOne) {
        this.cellsPlayerOne = new HashMap<>(cellsPlayerOne);
        return this;
    }

    public DataGame setCellsPlayerTwo(Map<Point, CellDataImpl> cellsPlayerTwo) {
        this.cellsPlayerTwo = new HashMap<>(cellsPlayerTwo);
        return this;
    }

    public DataGame setPawns(Map<Point, PawnDataImpl> pawns) {
        this.pawns = new HashMap<>(pawns);
        return this;
    }

    public DataGame setPawnsPlayerOne(Map<Point, PawnDataImpl> pawnsPlayerOne) {
        this.pawnsPlayerOne = new HashMap<>(pawnsPlayerOne);
        return this;
    }

    public DataGame setPawnsPlayerTwo(Map<Point, PawnDataImpl> pawnsPlayerTwo) {
        this.pawnsPlayerTwo = new HashMap<>(pawnsPlayerTwo);
        return this;
    }

    public Map<Point, PawnDataImpl> getPawnsPlayerTwo() {
        return pawnsPlayerTwo;
    }

    public void removePawnByPlayer(PawnDataImpl pawnData) {
        if (pawnData == null) return;
        if(pawnData.getPlayer() == CellState.PLAYER_ONE || pawnData.getPlayer() == CellState.PLAYER_ONE_KING){
            pawnsPlayerOne.remove(pawnData.getStartXY());
        } else if (pawnData.getPlayer() == CellState.PLAYER_TWO || pawnData.getPlayer() == CellState.PLAYER_TWO_KING){
            pawnsPlayerTwo.remove(pawnData.getStartXY());
        }
        pawns.remove(pawnData.getStartXY());
    }

    public void putPawnByPlayer(PawnDataImpl pawnData) {
        if (pawnData.getPlayer() == CellState.PLAYER_ONE || pawnData.getPlayer() == CellState.PLAYER_ONE_KING) {
            pawnsPlayerOne.put(pawnData.getStartXY(), pawnData);
        } else if (pawnData.getPlayer() == CellState.PLAYER_TWO || pawnData.getPlayer() == CellState.PLAYER_TWO_KING){
            pawnsPlayerTwo.put(pawnData.getStartXY(), pawnData);
        }
        pawns.put(pawnData.getStartXY(), pawnData);
    }

    public void putCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.getCellContain() == CellState.PLAYER_ONE || cellDataImpl.getCellContain() == CellState.PLAYER_ONE_KING) {
            cellsPlayerOne.put(cellDataImpl.getPointCell(), cellDataImpl);

        } else if (cellDataImpl.getCellContain() == CellState.PLAYER_TWO || cellDataImpl.getCellContain() == CellState.PLAYER_TWO_KING) {
            cellsPlayerTwo.put(cellDataImpl.getPointCell(), cellDataImpl);
        }
        cells.put(cellDataImpl.getPointCell(), cellDataImpl);
    }

    private void setCellInBoardCells(CellDataImpl cellDataImpl) {
        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
                if (boardCells[row][column].equals(cellDataImpl)){
                    boardCells[row][column] = new CellDataImpl(cellDataImpl);
                    break;
                }
            }
        }
    }

    public void removeCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.getCellContain() == CellState.PLAYER_ONE || cellDataImpl.getCellContain() == CellState.PLAYER_ONE_KING) {
            cellsPlayerOne.remove(cellDataImpl.getPointCell());
        } else if (cellDataImpl.getCellContain() == CellState.PLAYER_TWO || cellDataImpl.getCellContain() == CellState.PLAYER_TWO_KING) {
            cellsPlayerTwo.remove(cellDataImpl.getPointCell());
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
        if (pawnData == null || cellDataDst == null) return;
        removePawnByPlayer(pawnData);
        pawnData.setContainerCellXY(cellDataDst.getPointCell());
        pawnData.setStartXY(cellDataDst.getPointStartPawn());
        pawnData.setMasterPawn(pawnData.isMasterPawn() || cellDataDst.isMasterCell());
        pawnData.setPlayer(isPlayerOneTurn
                ? pawnData.isMasterPawn() || cellDataDst.isMasterCell() ? CellState.PLAYER_ONE_KING : CellState.PLAYER_ONE
                : pawnData.isMasterPawn() || cellDataDst.isMasterCell() ? CellState.PLAYER_TWO_KING : CellState.PLAYER_TWO);
        putPawnByPlayer(pawnData);
    }

    /**
     * Update the new cell
     *
     * @param cellData that need to be set
     * @param state is the current cell is empty or not
     */
    public void updateCell(CellDataImpl cellData, int state){
        // firstable remove the cell because after it the cell change the data/value
        removeCellByPlayer(cellData);
        cellData.setCellContain(state);
        putCellByPlayer(cellData);
        setCellInBoardCells(cellData);
//        cells.put(cellData.getPointCell(), cellData);
    }

    public PawnDataImpl getPawnByPoint(Point point) {
        return pawns.get(point);
    }

    public CellDataImpl getCellByPoint(Point point) {
        CellDataImpl cellData = cells.get(point);
        PawnDataImpl pawnData = getPawnByPoint(point);

        //        if (cellDataByPoint != null)
        return cellData != null
                ? cellData
                : pawnData != null ? cells.get(pawnData.getContainerCellXY()) : null;
    }

    public void updatePawnKilled(PawnDataImpl pawnData) {
        CellDataImpl cellByPoint = getCellByPoint(pawnData.getContainerCellXY());

        // update that the cell is change to empty because the pawn killed
        updateCell(cellByPoint, CellState.EMPTY);
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

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public void setPlayerTurn(boolean isPlayerOneTurnCopy) {
        this.isPlayerOneTurn = isPlayerOneTurnCopy;
    }

    public void setBoardCells(CellDataImpl[][] boardCells) {
        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
                this.boardCells[row][column] = new CellDataImpl(boardCells[row][column]);
            }
        }
    }

    public CellDataImpl[][] getBoardCells() {
        return boardCells;
    }

    public CellDataImpl[][] getCopyBoardCells() {
        CellDataImpl[][] board = new CellDataImpl[8][8];
        // set he next cel by cell
        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
                if (boardCells[row][column].getCellContain() == CellState.INVALID_STATE){
                    Log.d("TEST_GAME", "boardCells[row][column] POINT: " + boardCells[row][column].getPointCell());
                }
                board[row][column] = new CellDataImpl(boardCells[row][column]);
            }
        }
        return board;
    }

    public static class ColorCell{
        public static final int CHECKED_PAWN_START_END = Color.argb(155, 45, 170, 0);
        public static final int INVALID_CHECKED = Color.argb(155, 170, 0, 0);
        public static final int CAN_CELL_START = Color.argb(155, 243, 215, 0);
        public static final int INSIDE_PATH = Color.argb(155, 0, 43, 170);
        public static final int CLEAR_CHECKED = R.drawable.cell_1;
    }

    public class CellState {
        public static final int EMPTY_INVALID = -1;
        public static final int EMPTY = 0;
        public static final int PLAYER_ONE = 1;
        public static final int PLAYER_TWO = 2;
        public static final int PLAYER_ONE_KING = 11;
        public static final int PLAYER_TWO_KING = 22;
        public static final int INVALID_STATE = -999;
    }

    public class Mode{
        public static final int COMPUTER_GAME_MODE = 1;
        public static final int OFFLINE_GAME_MODE = 2;
        public static final int ONLINE_GAME_MODE = 3;
    }

    public class Direction{
        public static final int LEFT_TOP_DIRECTION = 1;
        public static final int LEFT_BOTTOM_DIRECTION = 2;
        public static final int RIGHT_TOP_DIRECTION = 3;
        public static final int RIGHT_BOTTOM_DIRECTION = 4;
    }


}