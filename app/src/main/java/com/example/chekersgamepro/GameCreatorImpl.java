package com.example.chekersgamepro;

import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.game_validation.GameValidationImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;

class GameCreatorImpl implements GameManager.ChangePlayerListener {

    private List<Point> cellsPointRelevantStart = new ArrayList<>();

    private Map<Point, Pair<List<Point>, List<PawnDataImpl>>> listsAllOptionalPathByCell = new HashMap<>();

    /**
     * The optional cells that the pawn can move on it
     */
    private List<Point> listOptionalCellsPathTmp = new ArrayList<>();//

    private List<PawnDataImpl> removeListPawnTmp = new ArrayList<>();

    private List<DataCellViewClick> dataOptionalPathByView = new ArrayList<>();//

    private DataGame dataGame = DataGame.getInstance();

    private GameValidationImpl gameValidation;

    private CellDataImpl prevCellData;

    private CellDataImpl prevCellDataTmp;

    private boolean isPlayerOneTurn;

    private CellDataImpl cellDataSrcCurrently;

    private boolean isAttackMove = false;

    public void clearData(){
        cellsPointRelevantStart.clear();

        listsAllOptionalPathByCell.clear();

        listOptionalCellsPathTmp.clear();

        dataOptionalPathByView.clear();

        isAttackMove = false;

    }

    public GameCreatorImpl(boolean isPlayerOneTurn
            , GameValidationImpl gameValidation
            , List<GameManager.ChangePlayerListener> changePlayerListListeners) {
        this.isPlayerOneTurn = isPlayerOneTurn;
        this.gameValidation = gameValidation;
        changePlayerListListeners.add(this);
    }

    public List<Point> createRelevantCellsStart() {
        cellsPointRelevantStart.clear();

        // Check if there is attack path
        isAttackMove = FluentIterable.from(isPlayerOneTurn
                ? dataGame.getPawnsPlayerOne().values()
                : dataGame.getPawnsPlayerTwo().values())
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(gameValidation::isAttackMove)
                .first()
                .isPresent();

        FluentIterable.from((isPlayerOneTurn
                    ? dataGame.getPawnsPlayerOne().values()
                    : dataGame.getPawnsPlayerTwo().values()))
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(gameValidation::isCanCellStart)
                .filter(cellData -> !isAttackMove || gameValidation.isAttackMove(cellData))
                .transform(CellDataImpl::getPoint)
                .transform(cellsPointRelevantStart::add)
                .toList();

        return cellsPointRelevantStart;
    }

    private CellDataImpl getNextCellByKing(CellDataImpl cellData, boolean isLeft){

        if (cellData != null){
            if (isLeft){
                if (cellData.getNextCellDataLeftPlayerOne() != null){
                    return cellData.getNextCellDataLeftPlayerOne();
                }

                if (cellData.getNextCellDataLeftPlayerTwo() != null){
                    return cellData.getNextCellDataLeftPlayerTwo();
                }
            } else {
                if (cellData.getNextCellDataRightPlayerOne() != null){
                    return cellData.getNextCellDataRightPlayerOne();
                }

                if (cellData.getNextCellDataRightPlayerTwo() != null){
                    return cellData.getNextCellDataRightPlayerTwo();
                }
            }
        }


        return null;

    }
    private boolean isMasterPawn;

    public List<DataCellViewClick> createOptionalPath(float x, float y){

        dataOptionalPathByView.clear();
        listsAllOptionalPathByCell.clear();
        listOptionalCellsPathTmp.clear();
        removeListPawnTmp.clear();

        CellDataImpl currCellData =  dataGame.getCellByPoint(new Point((int) x, (int) y));

        // check if the pawn is null
        // because if the user click when another process is running the pawn is null
        if (currCellData == null) return null;

        // check if point contains in the can be start cells
        if (!cellsPointRelevantStart.contains(currCellData.getPoint())) {
            addDataOptionalPath(false, currCellData);
            return dataOptionalPathByView;
        }

        isMasterPawn = dataGame.getPawnByPoint(currCellData.getPointStartPawn()).isMasterPawn();

        this.prevCellData = currCellData;
        this.cellDataSrcCurrently = currCellData;

        // add the first/root cell
        addDataOptionalPath(true, currCellData);


        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, true, isMasterPawn)){
            if (isMasterPawn){

                if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false, true)){
                    createOptionalPathByCell(currCellData.getNextCellDataLeftPlayerOne(), true);
                }

                if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false, true)){
                    createOptionalPathByCell(currCellData.getNextCellDataLeftPlayerTwo(), true);
                }

            } else {
                createOptionalPathByCell(getNextCell(currCellData, true), true);
            }

        }

        if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false, isMasterPawn)) {
            if (isMasterPawn) {
                if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false, true)) {
                    createOptionalPathByCell(currCellData.getNextCellDataRightPlayerOne(), true);
                }

                if (!isAttackMove || gameValidation.isAttackMoveByDirection(currCellData, false, true)) {
                    createOptionalPathByCell(currCellData.getNextCellDataRightPlayerTwo(), true);
                }
            } else {

                createOptionalPathByCell(getNextCell(currCellData, false), true);
            }
        }

        return dataOptionalPathByView;

    }



    private void createOptionalPathByCell(CellDataImpl currCellData, boolean isFromRoot){

        if (currCellData == null)return;

        // check the curr cell after the root if is empty is valid but the end.
        // if the curr cell is leaf is valid but the end
        if ((currCellData.isEmpty() && isFromRoot) || gameValidation.isLeaf(currCellData, isMasterPawn)){
            addDataOptionalPath(true, currCellData);
            listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());

            listsAllOptionalPathByCell.put(
                    currCellData.getPoint(), Pair.create(new ArrayList<>(listOptionalCellsPathTmp), new ArrayList<>(removeListPawnTmp)));
            listOptionalCellsPathTmp.clear();
            removeListPawnTmp.clear();
            return;
        } else {

            //check the cell after the root
            if (!currCellData.isEmpty() && isFromRoot && !gameValidation.isEqualPlayerCells(currCellData)){
                CellDataImpl nextCellDataByCell;

                //calculate the diagonal direction
                boolean isRightDiagonal = currCellData.getPoint().x > prevCellData.getPoint().x;

                if (isRightDiagonal){
                    nextCellDataByCell =  isMasterPawn ? getNextCellByKing(currCellData, false): getNextCell(currCellData, false);
                } else {
                    nextCellDataByCell = isMasterPawn ? getNextCellByKing(currCellData, true): getNextCell(currCellData, true);
                }
                if (nextCellDataByCell != null && nextCellDataByCell.isEmpty()){

                    addDataOptionalPath(true, currCellData);

                    removeListPawnTmp.add(dataGame.getPawnByPoint(currCellData.getPointStartPawn()));

                    prevCellData = currCellData;
                    createOptionalPathByCell(nextCellDataByCell, false);
                }
            }

            // check the cell is empty but not leaf(maybe it a node)
            // need to check the right and left next cell
            if (currCellData.isEmpty()){
                addDataOptionalPath(true, currCellData);
                listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());
                List<Point> listOptionalCellsPathTmpCopy = new ArrayList<>(listOptionalCellsPathTmp);

                prevCellData = currCellData;
                prevCellDataTmp = currCellData;
                CellDataImpl nextCellDataByCellRight = isMasterPawn ? getNextCellByKing(currCellData, false): getNextCell(currCellData, false);
                if (nextCellDataByCellRight != null && !nextCellDataByCellRight.isEmpty() && !gameValidation.isEqualPlayerCells(nextCellDataByCellRight)){
                    createOptionalPathByCell(nextCellDataByCellRight, true);
                }
                prevCellData = prevCellDataTmp;
                listOptionalCellsPathTmp = new ArrayList<>(listOptionalCellsPathTmpCopy);

                CellDataImpl nextCellDataByCellLeft = isMasterPawn ? getNextCellByKing(currCellData, true): getNextCell(currCellData, true);
                if (nextCellDataByCellLeft != null && !nextCellDataByCellLeft.isEmpty() && !gameValidation.isEqualPlayerCells(nextCellDataByCellLeft)) {
                    createOptionalPathByCell(nextCellDataByCellLeft, true);
                }
            }

        }

    }

    private CellDataImpl getNextCell(CellDataImpl cellData, boolean isLeft){
            return cellData != null
                    ? isPlayerOneTurn
                        ? isLeft
                            ? cellData.getNextCellDataLeftPlayerOne()
                            : cellData.getNextCellDataRightPlayerOne()
                        : isLeft
                            ? cellData.getNextCellDataLeftPlayerTwo()
                            : cellData.getNextCellDataRightPlayerTwo()
                    : null;

    }

    private void addDataOptionalPath(boolean isClickValid, @Nullable CellDataImpl cellData) {
        if (cellData == null)return;
        dataOptionalPathByView.add(new DataCellViewClick(isClickValid,cellData.getPoint(), dataOptionalPathByView.size() == 0, cellData.isEmpty()));
    }

    @Override
    public void onChangePlayer(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn;
    }

//    /**
//     * Check if the point is the end point in the path
//     * @param x from current point cell
//     * @param y from current point cell
//     * @return
//     */
//    public boolean isInOptionalPathValidCell(float x, float y) {
//
//      return listsAllOptionalPathByCell.get(new Point((int) x, (int) y)) != null;
//    }

    Point endPointPathFromUser;

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

    public void actionAfterPublishMovePawnPath(){
        indexRemovePawnList = 0;

        listPawnsNeededKilled = new ArrayList<>(listsAllOptionalPathByCell.get(endPointPathFromUser).second);

        setCurrentSrcDstCellData(dataGame.getCellByPoint(endPointPathFromUser));
    }

    public void setCurrentSrcDstCellData(CellDataImpl cellDataDst){
        dataGame.updateCell(cellDataDst, false, isPlayerOneTurn );
        // cell data src
        dataGame.updateCell(cellDataSrcCurrently, true, false);
        // pawn data src
        dataGame.updatePawn(dataGame.getPawnByPoint(cellDataSrcCurrently.getPointStartPawn()), cellDataDst);
    }

    private int indexRemovePawnList = 0;

    private PawnDataImpl currPawnDataNeededKilled;
    private List<PawnDataImpl> listPawnsNeededKilled;

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
