package com.example.chekersgamepro.data.data_game;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class DataGame extends DataGameHelper {

    public static final int GAME_BOARD_SIZE = 8;
    public static int DIFFICULT_LEVEL = 3;

    private static DataGame instance = null;

    private final int DIV_SIZE_CELL = 14;
    private final int COLOR_BORDER_CELL = Color.BLACK;
    private final int BORDER_WIDTH = 2;
    private int gameMode = -1;

    private Map<Integer, Point> idCells;

    private Map<Point, CellDataImpl> cells;
    private Map<Point, CellDataImpl> cellsPlayerOne;
    private Map<Point, CellDataImpl> cellsPlayerTwo;

    private Map<Point, PawnDataImpl> pawns;
    private Map<Point, PawnDataImpl> pawnsPlayerOne;
    private Map<Point, PawnDataImpl> pawnsPlayerTwo;

    private List<BorderLine> borderLines;

    private boolean isPlayerOneTurn;

    private int countKing;

    private CellDataImpl[][] boardCells;

    private int difficultLevelCustom = -1;

    public int getDifficultLevel(){
        if (difficultLevelCustom != -1){
            return Math.min(difficultLevelCustom, 5);
        }
        return DIFFICULT_LEVEL;
    }

    public void setDifficultLevel(int difficultLevelCustom){
        this.difficultLevelCustom = difficultLevelCustom;
    }

    private DataGame() {
        idCells = new HashMap<>();

        cells = new HashMap<>();
        cellsPlayerOne = new HashMap<>();
        cellsPlayerTwo = new HashMap<>();

        pawns = new HashMap<>();
        pawnsPlayerOne  = new HashMap<>();
        pawnsPlayerTwo = new HashMap<>();

        boardCells = new CellDataImpl[GAME_BOARD_SIZE][GAME_BOARD_SIZE];

        borderLines = new ArrayList<>();

        countKing = 0;
    }

    synchronized public static DataGame getInstance() {
        if(instance == null) {
            instance = new DataGame();
        }
        return instance;
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


    public void clearCachePawns() {
        pawns.clear();
        pawnsPlayerTwo.clear();
        pawnsPlayerOne.clear();
    }

    public void clearCacheCells() {
        cells.clear();
        cellsPlayerOne.clear();
        cellsPlayerTwo.clear();
    }

    public Map<Point, PawnDataImpl> getPawnsPlayerOne() {
        return pawnsPlayerOne;
    }

    public int getTotalRegularPawnsPlayerOne() {
        return pawnsPlayerOne.size() - getPawnsKingPlayerOne();
    }

    public int getPawnsKingPlayerOne() {
        countKing = 0;
        for (PawnDataImpl pawnData : pawnsPlayerOne.values()){
            if (pawnData.isMasterPawn()){
                countKing++;
            }
        }
        return countKing;
    }

    public int getPawnsKingPlayerTwo() {
        countKing = 0;
        for (PawnDataImpl pawnData : pawnsPlayerTwo.values()){
            if (pawnData.isMasterPawn()){
                countKing++;
            }
        }
        return countKing;
    }

    public int getTotalRegularPawnsPlayerTwo() {
        return pawnsPlayerTwo.size() - getPawnsKingPlayerTwo();
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
//        Log.d("TEST_GAME", "putPawnByPlayer: " + pawnData.getPlayer());
        pawns.put(pawnData.getStartXY(), pawnData);
    }

    public void putCellByPlayer(CellDataImpl cellDataImpl) {
        if (cellDataImpl.getCellContain() == CellState.PLAYER_ONE || cellDataImpl.getCellContain() == CellState.PLAYER_ONE_KING) {
            cellsPlayerOne.put(cellDataImpl.getPointCell(), cellDataImpl);

        } else if (cellDataImpl.getCellContain() == CellState.PLAYER_TWO || cellDataImpl.getCellContain() == CellState.PLAYER_TWO_KING) {
            cellsPlayerTwo.put(cellDataImpl.getPointCell(), cellDataImpl);
        }
        cells.put(cellDataImpl.getPointCell(), cellDataImpl);
//        Log.d("TEST_GAME", "DataGame -> SIZE CELLS: " + cells.size());
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
    }

    public PawnDataImpl getPawnByPoint(Point point) {
        return pawns.get(point);
    }

    public @Nullable CellDataImpl getCellByPoint(Point point) {
        CellDataImpl cellData = cells.get(point);
        PawnDataImpl pawnData = getPawnByPoint(point);

        //        if (cellDataByPoint != null)
        return cellData != null
                ? cellData
                : pawnData != null ? cells.get(pawnData.getContainerCellXY()) : null;
    }

    public Point getPointCellById(int id) {
       return idCells.get(id);
    }

    public void puCellById(int idCell, Point pointCell) {
        idCells.put(idCell, pointCell);
    }

    public void updatePawnKilled(PawnDataImpl pawnData) {
        CellDataImpl cellByPoint = getCellByPoint(pawnData.getContainerCellXY());

        if (cellByPoint == null) return;
        // update that the cell is change to empty because the pawn killed
        updateCell(cellByPoint, CellState.EMPTY);
        pawnData.setKilled(true);
        removePawnByPlayer(pawnData);
        removeCellByPlayer(cellByPoint);
    }


    public boolean isPlayerOneTurn() {
        return isPlayerOneTurn ;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

//    private RepositoryManager repositoryManager = RepositoryManager.create();

    public void setPlayerTurn(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn /*&& repositoryManager.getPlayer().isOwner() || repositoryManager.getPlayer().getNowPlay() == PlayersCode.PLAYER_ONE.ordinal()*/;
    }

    public void setBoardCells(CellDataImpl[][] boardCells) {
        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
//                this.boardCells[row][column] = new CellDataImpl(boardCells[row][column]);
                this.boardCells = boardCells;
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
                board[row][column] = new CellDataImpl(boardCells[row][column]);
            }
        }
        return board;
    }

    public List<BorderLine> getBorderLines() {
        return borderLines;
    }

    public int getColorBorderCell() {
        return COLOR_BORDER_CELL;
    }

    public int getBorderWidth() {
        return BORDER_WIDTH;
    }

    public void setBorderLines(List<BorderLine> borderLines) {
        this.borderLines = borderLines;
    }

    public static class ColorCell{
        public static final int CHECKED_PAWN_START_END = Color.parseColor("#FF2DAA00");//GREEN
        public static final int INVALID_CHECKED = Color.parseColor("#FFAA0000");//RED
        public static final int CAN_CELL_START = Color.parseColor("#FFF3D700");//YELLOW
        public static final int INSIDE_PATH = Color.parseColor("#FF002BAA");//BLUE
        public static final int CLEAR_CHECKED = R.drawable.cell_1;
    }

    public static class CellState {
        public static final int EMPTY_INVALID = -1;
        public static final int EMPTY = 0;
        public static final int PLAYER_ONE = 1;
        public static final int PLAYER_TWO = 2;
        public static final int PLAYER_ONE_KING = 11;
        public static final int PLAYER_TWO_KING = 22;
        public static final int INVALID_STATE = -999;
    }

    public static class Mode{
        public static final int COMPUTER_GAME_MODE = 1;
        public static final int OFFLINE_GAME_MODE = 2;
        public static final int ONLINE_GAME_MODE = 3;
    }

    public static class Direction{
        public static final int LEFT_TOP_DIRECTION = 1;
        public static final int LEFT_BOTTOM_DIRECTION = 2;
        public static final int RIGHT_TOP_DIRECTION = 3;
        public static final int RIGHT_BOTTOM_DIRECTION = 4;
    }
}