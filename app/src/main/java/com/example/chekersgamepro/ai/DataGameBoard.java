package com.example.chekersgamepro.ai;

import android.graphics.Point;
import android.util.Log;

import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.move.DataMove;
import com.example.chekersgamepro.data.move.Move;
import com.example.chekersgamepro.game_validation.GameValidationImpl;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataGameBoard {

    private DataGame dataGame = DataGame.getInstance();
    private CellDataImpl[][] boardCells = new CellDataImpl[DataGame.GAME_BOARD_SIZE][DataGame.GAME_BOARD_SIZE];
    private boolean isPlayerOneCurrently = true;
    private int playerOneCount = 0;
    private int playerTwoCount = 0;
    private int playerOneKingsCount = 0;
    private int playerTwoKingsCount = 0;
    private boolean isPlayerOneWinner;
    private AIComputerMove ai;
    private boolean isAttackMove = false;
    private GameValidationImpl gameValidation = new GameValidationImpl(this);
    private List<CellDataImpl> cellsListByPlayer = new ArrayList<>();
    private List<CellDataImpl> cellsRelevantStartList = new ArrayList<>();

    /**
     * List that all optional lists that include:
     * list Cell data that need to remove them(change to empty)
     */
    private List<DataMove> listsAllOptionalPathByCell = new ArrayList<>();

    private List<CellDataImpl> removeListPawnCell = new ArrayList<>();

    /**
     * All optional cells in the all optional lists
     */
    private List<DataCellViewClick> dataOptionalPathByView = new ArrayList<>();//

    private boolean isQueenPawn;
    private CellDataImpl prevCellData;
    private CellDataImpl cellDataSrcCurrently;

    public DataGameBoard() {
        ai = new AIComputerMove();
    }

    private DataGameBoard(CellDataImpl[][] boardCells, int playerOneCount, int playerTwoCount) {
        this();

        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = new CellDataImpl(boardCells[row][column]);
                this.boardCells[row][column] = new CellDataImpl(currCell);
            }
        }

        this.playerOneCount = playerOneCount;
        this.playerTwoCount = playerTwoCount;
    }

    public DataGameBoard(CellDataImpl[][] boardCells, int playerOneCount, int playerTwoCount, int playerOneKings, int playerTwoKings, boolean isPlayerOneCurrently) {
        this(boardCells, playerOneCount, playerTwoCount);

        this.playerOneKingsCount = playerOneKings;
        this.playerTwoKingsCount = playerTwoKings;
        this.gameValidation = new GameValidationImpl(this);
        this.isPlayerOneCurrently = isPlayerOneCurrently;
    }

    public boolean isPlayerOneCurrently() {
        return isPlayerOneCurrently;
    }

    public List<CellDataImpl> getCellsListByPlayer() {
        return cellsListByPlayer;
    }

    public List<CellDataImpl> getCellsRelevantStartList() {
        return cellsRelevantStartList;
    }

    public List<CellDataImpl> getRemoveListPawnCell() {
        return removeListPawnCell;
    }

    public List<DataCellViewClick> getDataOptionalPathByView() {
        return dataOptionalPathByView;
    }

    public boolean isQueenPawn() {
        return isQueenPawn;
    }


    public void clearData(){
        this.cellsRelevantStartList.clear();

        this.cellsListByPlayer.clear();

        this.listsAllOptionalPathByCell.clear();

        this.dataOptionalPathByView.clear();

        this.isAttackMove = false;
    }

    /**
     * @return the number of red pieces on the board
     */
    public int getPlayerOneCount() {
        return playerOneCount;
    }

    /**
     * @return the number of red kings on the board
     */
    public int getPlayerOneKingsCount() {
        return playerOneKingsCount;
    }

    /**
     * @return the number of black pieces on the board
     */
    public int getPlayerTwoCount() {
        return playerTwoCount;
    }

    /**
     * @return the number of black kings on the board
     */
    public int getPlayerTwoKingsCount() {
        return playerTwoKingsCount;
    }

    /**
     * @return the weighted score of the board
     */
    public float getPlayerOneScore() {
        return playerOneCount - playerOneKingsCount + (2.5f * playerOneKingsCount);
    }

    /**
     * @return the weighted score of the board
     */
    public float getPlayerTwoScore() {
        return playerTwoCount - playerTwoKingsCount + (2.5f * playerTwoKingsCount);
    }

    public @Nullable Move getBestMove(){
        this.boardCells = dataGame.getBoardCells();
        this.playerOneCount = dataGame.getPawnsPlayerOne().size();
        this.playerTwoCount = dataGame.getPawnsPlayerTwo().size();
        this.playerOneKingsCount = dataGame.getPawnsKingPlayerOne();
        this.playerTwoKingsCount = dataGame.getPawnsKingPlayerTwo();
        return  ai.getMoveAI(this, isPlayerOneCurrently)
                .setComputerTime();
    }

    public List<DataMove> createMovesByCellsStart(boolean isPlayerOnCurrently) {
        clearData();
        this.isPlayerOneCurrently = isPlayerOnCurrently;

        //create list that all cells by current player
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = new CellDataImpl(this.boardCells[row][column]);
                if ((currCell.getCellContain() == DataGame.CellState.PLAYER_ONE || currCell.getCellContain() == DataGame.CellState.PLAYER_ONE_KING ) && isPlayerOnCurrently){
                    cellsListByPlayer.add(currCell);
                } else if ((currCell.getCellContain() == DataGame.CellState.PLAYER_TWO || currCell.getCellContain() == DataGame.CellState.PLAYER_TWO_KING ) && !isPlayerOnCurrently){
                    cellsListByPlayer.add(currCell);
                }
            }
        }

        // Check if there is attack path
        isAttackMove = FluentIterable.from(cellsListByPlayer)
                .filter(gameValidation::isMoveAttack)
                .first()
                .isPresent();

        // create cells list can be start
        FluentIterable.from(cellsListByPlayer)
                .filter(this::isCanCellStart)
                .transform(cellsRelevantStartList::add)
                .toList();

        List<DataMove> cellDataListMoves = new ArrayList<>();

        // create cell data move list
        FluentIterable.from(cellsRelevantStartList)
                .transform(this::createOptionalPath)
                .transform(cellDataListMoves::addAll)
                .toList();

        return cellDataListMoves;

    }

    private boolean isCanCellStart(CellDataImpl cellData){
        boolean canCellStart = gameValidation.isCanCellStart(cellData);
        boolean isAttack = !isAttackMove || gameValidation.isMoveAttack(cellData);
        return canCellStart && isAttack;
    }

    public List<DataMove> createOptionalPath(CellDataImpl currCellSrc){

        dataOptionalPathByView.clear();
        listsAllOptionalPathByCell.clear();
        removeListPawnCell.clear();

        if (currCellSrc == null) return null;

        this.prevCellData = currCellSrc;
        this.cellDataSrcCurrently = new CellDataImpl(currCellSrc);
        this.isQueenPawn = gameValidation.isQueenPawn(currCellSrc);

        // add the first/root cell
        addDataOptionalPath( currCellSrc);

        // check and create left direction
        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellSrc, true)){

            if (isQueenPawn){
                if (!isAttackMove || gameValidation.isAttackMoveByQueenByDirection(currCellSrc.getNextCellDataLeftTop(), DataGame.Direction.LEFT_TOP_DIRECTION)){
                    createOptionalPathByCell(currCellSrc.getNextCellDataLeftTop(), true);
                }

                this.prevCellData = currCellSrc;

                if (!isAttackMove || gameValidation.isAttackMoveByQueenByDirection(currCellSrc.getNextCellDataLeftBottom(), DataGame.Direction.LEFT_BOTTOM_DIRECTION)) {
                    createOptionalPathByCell(currCellSrc.getNextCellDataLeftBottom(), true);
                }

                this.prevCellData = currCellSrc;

            } else {
                // add the first/root cell
                createOptionalPathByCell(getNextCell(currCellSrc, true, isPlayerOneCurrently), true);
            }

        }

        // check and create right direction
        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellSrc, false)){

            if (isQueenPawn){
                if (!isAttackMove || gameValidation.isAttackMoveByQueenByDirection(currCellSrc.getNextCellDataRightBottom(), DataGame.Direction.RIGHT_BOTTOM_DIRECTION)) {
                    createOptionalPathByCell(currCellSrc.getNextCellDataRightBottom(), true);
                }
                this.prevCellData = currCellSrc;

                if (!isAttackMove || gameValidation.isAttackMoveByQueenByDirection(currCellSrc.getNextCellDataRightTop(), DataGame.Direction.RIGHT_TOP_DIRECTION)) {
                    createOptionalPathByCell(currCellSrc.getNextCellDataRightTop(), true);
                }

            } else {
                // add the first/root cell
                createOptionalPathByCell(getNextCell(currCellSrc, false, isPlayerOneCurrently), true);
            }

        }

        return listsAllOptionalPathByCell;

    }

    private void createOptionalPathByCell(@Nullable CellDataImpl currCellData, boolean isFromRoot){

        if (currCellData == null)return;

        // check the curr cell after the root if is empty is valid but the end.
        // if the curr cell is leaf is valid but the end
        if ((currCellData.getCellContain() == DataGame.CellState.EMPTY && isFromRoot) || gameValidation.isLeaf(currCellData, dataOptionalPathByView, isQueenPawn)){
            addDataOptionalPath( currCellData);

            DataMove dataMove = new DataMove(new Move(cellDataSrcCurrently.getPointCell(), currCellData.getPointCell(), cellDataSrcCurrently.getIdCell())
                    , cellDataSrcCurrently
                    , currCellData
                    , new ArrayList<>(removeListPawnCell));

            listsAllOptionalPathByCell.add(dataMove);
            removeListPawnCell.clear();
            return;
        } else {

            //check the cell after the root
            if (currCellData.getCellContain() != DataGame.CellState.EMPTY && isFromRoot && !gameValidation.isEqualPlayersByRelevantData(currCellData)){
                CellDataImpl nextCellDataByCell = getCellDataByCell(dataGame.getNextCell(currCellData, prevCellData));

                if (nextCellDataByCell != null && nextCellDataByCell.getCellContain() == DataGame.CellState.EMPTY){

                    addDataOptionalPath( currCellData);

                    removeListPawnCell.add(currCellData);

                    prevCellData = currCellData;
                    createOptionalPathByCell(nextCellDataByCell, false);
                }
            }

            // check the cell is empty but not leaf(maybe it a node)
            // need to check the right and left next cell
            if (currCellData.getCellContain() == DataGame.CellState.EMPTY){

                if (isQueenPawn){
                    moveByQueenPawn(currCellData);
                } else {
                    moveByRegularPawn(currCellData);
                }

            }

        }

    }

    private void moveByQueenPawn(CellDataImpl currCellData){
        addDataOptionalPath( currCellData);

        List<CellDataImpl> removeListPawnCopy = new ArrayList<>(removeListPawnCell);

        prevCellData = currCellData;
        CellDataImpl prevCellDataTmp = copyObject(currCellData);

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null
                && !gameValidation.isAlreadyExists(nextCellDataLeftBottom, dataOptionalPathByView) && nextCellDataLeftBottom.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayersByRelevantData(nextCellDataLeftBottom)){
            createOptionalPathByCell(nextCellDataLeftBottom, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        removeListPawnCell = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if ( nextCellDataRightBottom != null
                && !gameValidation.isAlreadyExists(nextCellDataRightBottom, dataOptionalPathByView) && nextCellDataRightBottom.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayersByRelevantData(nextCellDataRightBottom)){
            createOptionalPathByCell(nextCellDataRightBottom, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        removeListPawnCell = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if ( nextCellDataLeftTop != null
                && !gameValidation.isAlreadyExists(nextCellDataLeftTop, dataOptionalPathByView) && nextCellDataLeftTop.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayersByRelevantData(nextCellDataLeftTop)){
            createOptionalPathByCell(nextCellDataLeftTop, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        removeListPawnCell = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if ( nextCellDataRightTop != null
                && !gameValidation.isAlreadyExists(nextCellDataRightTop, dataOptionalPathByView) && nextCellDataRightTop.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayersByRelevantData(nextCellDataRightTop)){
            createOptionalPathByCell(nextCellDataRightTop, true);
        }

    }

    private void moveByRegularPawn(CellDataImpl currCellData){
        addDataOptionalPath( currCellData);

        List<CellDataImpl> removeListPawnCopy = new ArrayList<>(removeListPawnCell);

        prevCellData = currCellData;
        CellDataImpl prevCellDataTmp = copyObject(currCellData);

        CellDataImpl nextCellDataByCellRight = getCellDataByCell(dataGame.getNextCell(currCellData, false, isPlayerOneCurrently));
        if (nextCellDataByCellRight != null && nextCellDataByCellRight.getCellContain() != DataGame.CellState.EMPTY && !gameValidation.isEqualPlayersByRelevantData(nextCellDataByCellRight)){
            createOptionalPathByCell(nextCellDataByCellRight, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        removeListPawnCell = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataByCellLeft = getNextCell(currCellData, true, isPlayerOneCurrently);
        if (nextCellDataByCellLeft != null && nextCellDataByCellLeft.getCellContain() != DataGame.CellState.EMPTY && !gameValidation.isEqualPlayersByRelevantData(nextCellDataByCellLeft)) {
            createOptionalPathByCell(nextCellDataByCellLeft, true);
        }
    }

    private CellDataImpl copyObject(CellDataImpl cellData){

        return new CellDataImpl(cellData);

    }

    private void addDataOptionalPath( @Nullable CellDataImpl cellData) {
        if (cellData == null)return;
        dataOptionalPathByView.add(new DataCellViewClick(cellData.getPointCell()));
    }

    /**
     *
     *
     *   point of the dst cell
     * @return
     */
    public void setMovePawnPath(DataMove dataMove, boolean isPlayerOnCurrently) {

        removeCellIfNeeded(dataMove.getCellsListNeedRemove());
        setCurrentSrcDstCellData(dataMove.getCellDataSrc(), dataMove.getCellDataDst(), isPlayerOnCurrently);

    }

    public void setCurrentSrcDstCellData(CellDataImpl cellDataSrc, CellDataImpl cellDataDst, boolean isPlayerOnCurrently){

        int stateCell = -999;
        if (isPlayerOnCurrently){
            if (cellDataDst.isMasterCell() || cellDataSrc.getCellContain() == DataGame.CellState.PLAYER_ONE_KING){
                stateCell = DataGame.CellState.PLAYER_ONE_KING;
            } else if (cellDataSrc.getCellContain() == DataGame.CellState.PLAYER_ONE){
                stateCell = DataGame.CellState.PLAYER_ONE;
            }
        } else {
            if (cellDataDst.isMasterCell() || cellDataSrc.getCellContain() == DataGame.CellState.PLAYER_TWO_KING){
                stateCell = DataGame.CellState.PLAYER_TWO_KING;
            } else if (cellDataSrc.getCellContain() == DataGame.CellState.PLAYER_TWO){
                stateCell = DataGame.CellState.PLAYER_TWO;
            }
        }

        // set cell data dst
        cellDataDst.setCellContain(stateCell);
        // set cell data src
        cellDataSrc.setCellContain(DataGame.CellState.EMPTY);

        updateCell(cellDataDst);
        updateCell(cellDataSrc);

        calculateScoreBoard();
    }

    private void updateCell(CellDataImpl cellData) {

        // update
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = this.boardCells[row][column];
                if (currCell.getPointCell().x == cellData.getPointCell().x
                        && currCell.getPointCell().y == cellData.getPointCell().y) {
                    this.boardCells[row][column] = new CellDataImpl(cellData);
                    break;
                }

            }
        }

    }

    private void calculateScoreBoard(){
        clearScore();

        //calculate the score board
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = this.boardCells[row][column];

                if (currCell.getCellContain() == DataGame.CellState.PLAYER_ONE){
                    playerOneCount +=1;

                } else if (currCell.getCellContain() == DataGame.CellState.PLAYER_ONE_KING){
                    playerOneKingsCount +=1;

                }  else if (currCell.getCellContain() == DataGame.CellState.PLAYER_TWO){
                    playerTwoCount +=1;

                } else if (currCell.getCellContain() == DataGame.CellState.PLAYER_TWO_KING){
                    playerTwoKingsCount +=1;

                }

            }
        }

    }

    public void printTotalPawns(){
        Log.d("TEST_GAME", "playerOneCount: " + playerOneCount);
        Log.d("TEST_GAME", "playerOneKingsCount: " + playerOneKingsCount);
        Log.d("TEST_GAME", "playerTwoCount: " + playerTwoCount);
        Log.d("TEST_GAME", "playerTwoKingsCount: " + playerTwoKingsCount);
    }

    private void clearScore(){
        playerOneCount = 0;
        playerTwoCount = 0;
        playerOneKingsCount = 0;
        playerTwoKingsCount = 0;
    }

    public List<DataMove> getListsAllOptionalPathByCell(){
        return listsAllOptionalPathByCell;
    }

    /**
     * Remove the relevant pawn from the data
     * the point param is the current point(pawn),
     * and check if the next cell in the list is container the pawn tat need to be removed
     *
     * @return
     */
    public void removeCellIfNeeded(List<CellDataImpl> listCellsNeededKilled) {
        FluentIterable
                .from(listCellsNeededKilled)
                .transform(this::updateCellKilled)
                .toList();
    }

    private Object updateCellKilled(CellDataImpl cellData) {
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = this.boardCells[row][column];
                if (currCell.getPointCell().x == cellData.getPointCell().x
                        && currCell.getPointCell().y == cellData.getPointCell().y) {
                    currCell.setCellContain(DataGame.CellState.EMPTY);
                    this.boardCells[row][column] = new CellDataImpl(currCell);
                    return new Object();
                }

            }
        }
        return new Object();
    }

    public CellDataImpl getCellDataByCell(@Nullable CellDataImpl cellData) {
        if (cellData == null) return null;
        Point point = cellData.getPointCell();
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < DataGame.GAME_BOARD_SIZE; column++) {
                CellDataImpl currCell = this.boardCells[row][column];

                if (currCell.getPointCell().x == point.x
                        && currCell.getPointCell().y == point.y) {
                    return this.boardCells[row][column];
                }

            }
        }
        return new CellDataImpl();
    }

    public CellDataImpl getDataCell(int row, int col) {
        return boardCells[row][col];
    }

    public CellDataImpl getNextCell(CellDataImpl cellData, boolean isLeft, boolean isPlayerOneTurn) {
        return cellData != null
                ? isPlayerOneTurn
                    ? isLeft
                        ? getCellDataByCell(cellData.getNextCellDataLeftBottom())
                        : getCellDataByCell(cellData.getNextCellDataRightBottom())
                    : isLeft
                        ? getCellDataByCell(cellData.getNextCellDataLeftTop())
                        : getCellDataByCell(cellData.getNextCellDataRightTop())
                : null;

    }

    /**
     * Creates a new board with the same information as the given board
     * @return a copy of the given board
     */
    public DataGameBoard getCopyBoard() {
        CellDataImpl[][] cellsDataBard = new CellDataImpl[DataGame.GAME_BOARD_SIZE][DataGame.GAME_BOARD_SIZE];
        for (int row = 0; row < DataGame.GAME_BOARD_SIZE; row++) {
            for (int col = 0; col < DataGame.GAME_BOARD_SIZE; col++) {
                CellDataImpl dataCell = getDataCell(row, col);
                cellsDataBard[row][col] = new CellDataImpl(dataCell);
            }
        }

        return new DataGameBoard(cellsDataBard
                , getPlayerOneCount()
                , getPlayerTwoCount()
                , getPlayerOneKingsCount()
                , getPlayerTwoKingsCount()
                , isPlayerOneCurrently());
    }

    /**
     * Scores the given board based on a weighted system
     * @return the score of the given board
     */
    public float getEvaluateBoard(boolean isPlayerOne) {

//        if (isPlayerOne) {
            return getPlayerOneScore() - getPlayerTwoScore();
//        } else {
//            return getPlayerTwoScore() - getPlayerOneScore();
//        }
    }

}