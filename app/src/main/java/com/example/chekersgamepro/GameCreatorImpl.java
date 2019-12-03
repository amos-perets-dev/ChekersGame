package com.example.chekersgamepro;

import android.graphics.Point;
import android.util.Pair;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.game_validation.GameValidationImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.example.chekersgamepro.data_game.DataGame;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GameCreatorImpl{

    private List<DataCellViewClick> cellsPointRelevantStart = new ArrayList<>();

    private Map<Point, Pair<List<Point>, List<PawnDataImpl>>> listsAllOptionalPathByCell = new HashMap<>();

    /**
     * The optional cells that the pawn can move on it
     */
    private List<Point> listOptionalPawnMovePathTmp = new ArrayList<>();//

    private List<PawnDataImpl> removeListPawn = new ArrayList<>();

    private List<DataCellViewClick> dataOptionalPathByView = new ArrayList<>();//

    private DataGame dataGame = DataGame.getInstance();

    private GameValidationImpl gameValidation;

    private CellDataImpl prevCellData;

    private CellDataImpl cellDataSrcCurrently;

    private boolean isAttackMove = false;

    private boolean isQueenPawn;

    private Point endPointPathFromUser;

    private int indexRemovePawnList = 0;

    private PawnDataImpl currPawnDataNeededKilled;

    private List<PawnDataImpl> listPawnsNeededKilled;

    public void clearData(){
//        Log.d("TEST_GAME", "clearData CLEAR LIST");
        cellsPointRelevantStart.clear();

        listsAllOptionalPathByCell.clear();

        listOptionalPawnMovePathTmp.clear();

        dataOptionalPathByView.clear();

        isAttackMove = false;

    }

    public GameCreatorImpl(GameValidationImpl gameValidation) {
        this.gameValidation = gameValidation;
    }

    /**
     * Create list that all start cells
     *
     * @return
     */
    public List<DataCellViewClick> createRelevantCellsStart() {
        cellsPointRelevantStart.clear();

        FluentIterable<CellDataImpl> cellsCanBeMaybeStart = FluentIterable.from((dataGame.isPlayerOneTurn()
                ? dataGame.getCellsPlayerOne().values()
                : dataGame.getCellsPlayerTwo().values()));

        // Check if there is attack path
        isAttackMove = cellsCanBeMaybeStart
                .filter(gameValidation::isMoveAttack)
                .first()
                .isPresent();

        cellsCanBeMaybeStart
                .filter(gameValidation::isCanCellStart)
                .filter(cellData -> !isAttackMove || gameValidation.isMoveAttack(cellData))
                .transform(CellDataImpl::getPointCell)
                .transform(point -> new DataCellViewClick (point, DataGame.ColorCell.CAN_CELL_START, DataGame.ColorCell.CLEAR_CHECKED))
                .transform(cellsPointRelevantStart::add)
                .toList();


        return cellsPointRelevantStart;
    }

    private FluentIterable<CellDataImpl> getCellsData() {
        return FluentIterable.from((dataGame.isPlayerOneTurn()
                ? dataGame.getPawnsPlayerOne().values()
                : dataGame.getPawnsPlayerTwo().values()))
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint);
    }

    public List<DataCellViewClick> getCellsPointRelevantStart() {
        return cellsPointRelevantStart;
    }

    public List<DataCellViewClick> createOptionalPath(float x, float y){

        dataOptionalPathByView.clear();
        listsAllOptionalPathByCell.clear();
        listOptionalPawnMovePathTmp.clear();
        removeListPawn.clear();

        CellDataImpl currCellData =  dataGame.getCellByPoint(new Point((int) x, (int) y));

        // check if the pawn is null
        // because if the user click when another process is running the pawn is null
        if (currCellData == null) return null;

        // check if point contains in the can be start cells
        boolean isPointInRelevantCell = FluentIterable.from(cellsPointRelevantStart)
                .transform(DataCellViewClick::getPoint)
                .filter(point -> point.x == currCellData.getPointCell().x && point.y == currCellData.getPointCell().y)
                .first()
                .isPresent();
        if (!isPointInRelevantCell) {
            addDataOptionalPath( currCellData, DataGame.ColorCell.INVALID_CHECKED, DataGame.ColorCell.CLEAR_CHECKED);
            return dataOptionalPathByView;
        }

        this.prevCellData = currCellData;
        this.cellDataSrcCurrently = currCellData;
        this.isQueenPawn = gameValidation.isQueenPawn(currCellData);

        // add the first/root cell
        addDataOptionalPath( currCellData, DataGame.ColorCell.CHECKED_PAWN_START_END, DataGame.ColorCell.CAN_CELL_START);

        // check and create left direction
        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, true)){

            if (isQueenPawn){
                if (!isAttackMove
                        || gameValidation.isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftTop(), DataGame.Direction.LEFT_TOP_DIRECTION)){
                    createOptionalPathByCell(currCellData.getNextCellDataLeftTop(), true);
                }

                this.prevCellData = currCellData;

                if (!isAttackMove
                        || gameValidation.isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftBottom(), DataGame.Direction.LEFT_BOTTOM_DIRECTION)) {
                    createOptionalPathByCell(currCellData.getNextCellDataLeftBottom(), true);
                }

                this.prevCellData = currCellData;

            } else {
                // add the first/root cell
                createOptionalPathByCell(dataGame.getNextCell(currCellData, true, dataGame.isPlayerOneTurn()), true);
            }

        }

        // check and create right direction
        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false)){

            if (isQueenPawn){
                if (!isAttackMove
                        || gameValidation.isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightBottom(), DataGame.Direction.RIGHT_BOTTOM_DIRECTION)) {
                    createOptionalPathByCell(currCellData.getNextCellDataRightBottom(), true);
                }
                this.prevCellData = currCellData;

                if (!isAttackMove
                        || gameValidation.isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightTop(), DataGame.Direction.RIGHT_TOP_DIRECTION)) {
                    createOptionalPathByCell(currCellData.getNextCellDataRightTop(), true);
                }

            } else {
                // add the first/root cell
                createOptionalPathByCell(dataGame.getNextCell(currCellData, false, dataGame.isPlayerOneTurn()), true);
            }

        }

        return dataOptionalPathByView;

    }



    private void createOptionalPathByCell(@Nullable CellDataImpl currCellData, boolean isFromRoot){

        if (currCellData == null)return;

        // check the curr cell after the root if is empty is valid but the end.
        // if the curr cell is leaf is valid but the end
        if ((currCellData.getCellContain() == DataGame.CellState.EMPTY && isFromRoot) || gameValidation.isLeaf(currCellData, dataOptionalPathByView, isQueenPawn)){
            addDataOptionalPath( currCellData, DataGame.ColorCell.CHECKED_PAWN_START_END, DataGame.ColorCell.CLEAR_CHECKED);
            listOptionalPawnMovePathTmp.add(currCellData.getPointStartPawn());

            listsAllOptionalPathByCell.put(
                    currCellData.getPointCell(), Pair.create(new ArrayList<>(listOptionalPawnMovePathTmp), new ArrayList<>(removeListPawn)));
            listOptionalPawnMovePathTmp.clear();
            removeListPawn.clear();
            return;
        } else {

            //check the cell after the root
            if (currCellData.getCellContain() != DataGame.CellState.EMPTY && isFromRoot && !gameValidation.isEqualPlayerCells(currCellData)){
                CellDataImpl nextCellDataByCell = dataGame.getNextCell(currCellData, prevCellData);

                if (nextCellDataByCell != null && nextCellDataByCell.getCellContain() == DataGame.CellState.EMPTY){

                    addDataOptionalPath( currCellData, DataGame.ColorCell.INSIDE_PATH, DataGame.ColorCell.CLEAR_CHECKED);

                    removeListPawn.add(dataGame.getPawnByPoint(currCellData.getPointStartPawn()));

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

    private boolean isAlreadyExists(CellDataImpl currCellData){
        return FluentIterable.from(dataOptionalPathByView)
                .transform(DataCellViewClick::getPoint)
                .filter(point -> currCellData.getPointCell().x == point.x && currCellData.getPointCell().y == point.y)
                .first()
                .isPresent();
    }

    private void moveByQueenPawn(CellDataImpl currCellData){
        addDataOptionalPath( currCellData, DataGame.ColorCell.INSIDE_PATH, DataGame.ColorCell.CLEAR_CHECKED);
        listOptionalPawnMovePathTmp.add(currCellData.getPointStartPawn());

        List<Point> listOptionalCellsPathTmpCopy = new ArrayList<>(listOptionalPawnMovePathTmp);
        List<PawnDataImpl> removeListPawnCopy = new ArrayList<>(removeListPawn);

        prevCellData = currCellData;
        CellDataImpl prevCellDataTmp = copyObject(currCellData);

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null
                && !isAlreadyExists(nextCellDataLeftBottom) && nextCellDataLeftBottom.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataLeftBottom)){
            createOptionalPathByCell(nextCellDataLeftBottom, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        listOptionalPawnMovePathTmp = new ArrayList<>(listOptionalCellsPathTmpCopy);
        removeListPawn = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if ( nextCellDataRightBottom != null
                && !isAlreadyExists(nextCellDataRightBottom) && nextCellDataRightBottom.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataRightBottom)){
            createOptionalPathByCell(nextCellDataRightBottom, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        listOptionalPawnMovePathTmp = new ArrayList<>(listOptionalCellsPathTmpCopy);
        removeListPawn = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if ( nextCellDataLeftTop != null
                && !isAlreadyExists(nextCellDataLeftTop) && nextCellDataLeftTop.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataLeftTop)){
            createOptionalPathByCell(nextCellDataLeftTop, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        listOptionalPawnMovePathTmp = new ArrayList<>(listOptionalCellsPathTmpCopy);
        removeListPawn = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if ( nextCellDataRightTop != null
                && !isAlreadyExists(nextCellDataRightTop) && nextCellDataRightTop.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataRightTop)){
            createOptionalPathByCell(nextCellDataRightTop, true);
        }

    }

    private void moveByRegularPawn(CellDataImpl currCellData){
        addDataOptionalPath( currCellData, DataGame.ColorCell.INSIDE_PATH, DataGame.ColorCell.CLEAR_CHECKED);
        listOptionalPawnMovePathTmp.add(currCellData.getPointStartPawn());

        List<Point> listOptionalCellsPathTmpCopy = new ArrayList<>(listOptionalPawnMovePathTmp);
        List<PawnDataImpl> removeListPawnCopy = new ArrayList<>(removeListPawn);

        prevCellData = currCellData;
        CellDataImpl prevCellDataTmp = copyObject(currCellData);

        CellDataImpl nextCellDataByCellRight = dataGame.getNextCell(currCellData, false, dataGame.isPlayerOneTurn());
        if (nextCellDataByCellRight != null
                && nextCellDataByCellRight.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataByCellRight)){
            createOptionalPathByCell(nextCellDataByCellRight, true);
        }

        prevCellData = copyObject(prevCellDataTmp);
        listOptionalPawnMovePathTmp = new ArrayList<>(listOptionalCellsPathTmpCopy);
        removeListPawn = new ArrayList<>(removeListPawnCopy);

        CellDataImpl nextCellDataByCellLeft = dataGame.getNextCell(currCellData, true, dataGame.isPlayerOneTurn());
        if (nextCellDataByCellLeft != null
                && nextCellDataByCellLeft.getCellContain() != DataGame.CellState.EMPTY
                && !gameValidation.isEqualPlayerCells(nextCellDataByCellLeft)) {
            createOptionalPathByCell(nextCellDataByCellLeft, true);
        }
    }

    private CellDataImpl copyObject(CellDataImpl cellData){

        return new CellDataImpl(cellData);

    }

    private void addDataOptionalPath( @Nullable CellDataImpl cellData, int colorChecked, int colorClearChecked) {
        if (cellData == null)return;
        dataOptionalPathByView.add(new DataCellViewClick(cellData.getPointCell(), colorChecked, colorClearChecked));
    }

    /**
     *
     *
     * @param x point of the dst cell
     * @param y point of the dst cell
     * @return
     */
    public List<Point> getMovePawnPath(float x, float y) {
        //check if the point in the path and in the valid cell(end point)
        if (listsAllOptionalPathByCell.get(new Point((int) x, (int) y)) == null) return null;

        endPointPathFromUser = new Point((int) x, (int) y);

        return listsAllOptionalPathByCell.get(endPointPathFromUser).first;
    }

    public Set<Point> getOptionalPointsListComputer(){
        return listsAllOptionalPathByCell.keySet();
    }

    public void actionAfterPublishMovePawnPath(){
        indexRemovePawnList = 0;

        listPawnsNeededKilled = new ArrayList<>(listsAllOptionalPathByCell.get(endPointPathFromUser).second);

        setCurrentSrcDstCellData(dataGame.getCellByPoint(endPointPathFromUser));
    }

    public void setCurrentSrcDstCellData(CellDataImpl cellDataDst){
//        Log.d("TEST_GAME", "GameCreatorImpl -> setCurrentSrcDstCellData -> cellDataDst: " + cellDataDst.toString());
        // set cell data dst
        dataGame.updateCell(cellDataDst
                , dataGame.isPlayerOneTurn()
                        ? cellDataDst.isMasterCell() || cellDataSrcCurrently.getCellContain() == DataGame.CellState.PLAYER_ONE_KING ? DataGame.CellState.PLAYER_ONE_KING : DataGame.CellState.PLAYER_ONE
                        : cellDataDst.isMasterCell() || cellDataSrcCurrently.getCellContain() == DataGame.CellState.PLAYER_TWO_KING ? DataGame.CellState.PLAYER_TWO_KING : DataGame.CellState.PLAYER_TWO);
        // set cell data src
//        Log.d("TEST_GAME", "GameCreatorImpl -> setCurrentSrcDstCellData -> cellDataSrc: " + cellDataSrcCurrently.toString());
        dataGame.updateCell(cellDataSrcCurrently, DataGame.CellState.EMPTY);
        // pawn data src
        dataGame.updatePawn(dataGame.getPawnByPoint(cellDataSrcCurrently.getPointStartPawn()), cellDataDst);
    }

    /**
     * Remove the relevant pawn from the data
     * the point param is the current point(pawn),
     * and check if the next cell in the list is container the pawn tat need to be removed
     *
     * @return
     */
    public PawnDataImpl removePawnIfNeeded() {

        PawnDataImpl pawnData = null;
        if (indexRemovePawnList < listPawnsNeededKilled.size()){
            pawnData = listPawnsNeededKilled.get(indexRemovePawnList);
            indexRemovePawnList++;
            this.currPawnDataNeededKilled = pawnData;
        }
        return pawnData;

    }

    public void updatePawnKilled() {
        dataGame.updatePawnKilled(currPawnDataNeededKilled);
    }

}
