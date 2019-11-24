package com.example.chekersgamepro.data.game_validation;

import android.graphics.Point;

import com.example.chekersgamepro.DataCellViewClick;
import com.example.chekersgamepro.data_game.DataGame;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.google.common.collect.FluentIterable;

import java.util.List;

public class GameValidationImpl {

    private DataGame dataGame = DataGame.getInstance();

    public GameValidationImpl() {

    }

//    private boolean isCanCellStartByQueen(CellDataImpl currCellData){
//
//        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
//        CellDataImpl nextCellByNextLeftBottom = dataGame.getNextCellByDirection(nextCellDataLeftBottom, DataGame.LEFT_BOTTOM_DIRECTION);
//        if (nextCellDataLeftBottom != null){
//            if (nextCellDataLeftBottom.isEmpty()
//                    || !nextCellDataLeftBottom.isEmpty() && !isEqualPlayerCells(nextCellDataLeftBottom)
//                    && nextCellByNextLeftBottom != null && nextCellByNextLeftBottom.isEmpty()) {
//                return true;
//            }
//        }
//
//        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
//        CellDataImpl nextCellByNextLeftTop = dataGame.getNextCellByDirection(nextCellDataLeftTop, DataGame.LEFT_TOP_DIRECTION);
//        if (nextCellDataLeftTop != null){
//            if (nextCellDataLeftTop.isEmpty()
//                    || !nextCellDataLeftTop.isEmpty() && !isEqualPlayerCells(nextCellDataLeftTop)
//                    && nextCellByNextLeftTop != null && nextCellByNextLeftTop.isEmpty()) {
//                return true;
//            }
//        }
//
//        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
//        CellDataImpl nextCellByNextRightBottom = dataGame.getNextCellByDirection(nextCellDataLeftBottom, DataGame.RIGHT_BOTTOM_DIRECTION);
//        if (nextCellDataRightBottom != null){
//            if (nextCellDataRightBottom.isEmpty()
//                    || !nextCellDataRightBottom.isEmpty() && !isEqualPlayerCells(nextCellDataRightBottom)
//                    && nextCellByNextRightBottom != null && nextCellByNextRightBottom.isEmpty()) {
//                return true;
//            }
//        }
//
//        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
//        CellDataImpl nextCellByNextRightTop = dataGame.getNextCellByDirection(nextCellDataLeftBottom, DataGame.RIGHT_TOP_DIRECTION);
//        if (nextCellDataRightTop != null){
//            if (nextCellDataRightTop.isEmpty()
//                    || !nextCellDataRightTop.isEmpty() && !isEqualPlayerCells(nextCellDataRightTop)
//                    && nextCellByNextRightTop != null && nextCellByNextRightTop.isEmpty()) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }

    private boolean isCanCellStartByQueen(CellDataImpl currCellData){

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null){
            CellDataImpl nextCellByNextLeftBottom = nextCellDataLeftBottom.getNextCellDataLeftBottom();
            if (nextCellDataLeftBottom.isEmpty()
                    || !nextCellDataLeftBottom.isEmpty() && !isEqualPlayerCells(nextCellDataLeftBottom)
                    && nextCellByNextLeftBottom != null && nextCellByNextLeftBottom.isEmpty()) {
                return true;
            }
        }

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if (nextCellDataLeftTop != null){
            CellDataImpl nextCellByNextLeftTop = nextCellDataLeftTop.getNextCellDataLeftTop();
            if (nextCellDataLeftTop.isEmpty()
                    || !nextCellDataLeftTop.isEmpty() && !isEqualPlayerCells(nextCellDataLeftTop)
                    && nextCellByNextLeftTop != null && nextCellByNextLeftTop.isEmpty()) {
                return true;
            }
        }

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if (nextCellDataRightBottom != null){
            CellDataImpl nextCellByNextRightBottom = nextCellDataRightBottom.getNextCellDataRightBottom();
            if (nextCellDataRightBottom.isEmpty()
                    || !nextCellDataRightBottom.isEmpty() && !isEqualPlayerCells(nextCellDataRightBottom)
                    && nextCellByNextRightBottom != null && nextCellByNextRightBottom.isEmpty()) {
                return true;
            }
        }

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if (nextCellDataRightTop != null){
            CellDataImpl nextCellByNextRightTop = nextCellDataRightTop.getNextCellDataRightTop();
            if (nextCellDataRightTop.isEmpty()
                    || !nextCellDataRightTop.isEmpty() && !isEqualPlayerCells(nextCellDataRightTop)
                    && nextCellByNextRightTop != null && nextCellByNextRightTop.isEmpty()) {
                return true;
            }
        }

        return false;

    }


    public boolean isCanCellStart(CellDataImpl currCellData){

        if (isQueenPawn(currCellData)){
            return isCanCellStartByQueen(currCellData);
        }

        //Check the left direction
        CellDataImpl nextCellLeft = dataGame.getNextCell(currCellData, true);
        // 1. check if there is normal turn
        if (nextCellLeft != null && nextCellLeft.isEmpty()){
            return true;
        }

        CellDataImpl nextCellChildLeft = dataGame.getNextCell(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = dataGame.getNextCell(currCellData, false);
        // 1. check if there is normal turn
        if (nextCellRight != null && nextCellRight.isEmpty()){
            return true;
        }

        CellDataImpl nextCellChildRight = dataGame.getNextCell(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
            return true;
        }

       return false;
    }


    public boolean isLeaf(CellDataImpl currCell, List<DataCellViewClick> dataOptionalPathByView, boolean isQueenPawn){

        if (!currCell.isEmpty()){
            return false;
        }

        if (isQueenPawn){
            return isLeafByQueen(currCell, dataOptionalPathByView);
        }

        CellDataImpl nextCellLeft = dataGame.getNextCell(currCell, true);

        // check if there is attack path
        if (nextCellLeft != null){
            CellDataImpl nextCellLeftByNextCell = dataGame.getNextCell(nextCellLeft, true);
            if (nextCellLeftByNextCell != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellLeftByNextCell.isEmpty()){
                return false;
            }
        }

        CellDataImpl nextCellRight = dataGame.getNextCell(currCell, false);

        // check if there is attack path
        if (nextCellRight != null){
            CellDataImpl nextCellRightByNextCell = dataGame.getNextCell(nextCellRight, false);
            if (nextCellRightByNextCell != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellRightByNextCell.isEmpty()){
                return false;
            }
        }

        return true;

    }

    private boolean isAlreadyExistsInList(CellDataImpl currCellData, List<DataCellViewClick> dataOptionalPathByView){
        return FluentIterable.from(dataOptionalPathByView)
                .transform(DataCellViewClick::getPoint)
                .filter(point -> currCellData.getPoint().x == point.x && currCellData.getPoint().y == point.y)
                .first()
                .isPresent();
    }


    private boolean isLeafByQueen(CellDataImpl currCellData, List<DataCellViewClick> dataOptionalPathByView) {

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null && !isAlreadyExistsInList(nextCellDataLeftBottom, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataLeftBottom, DataGame.LEFT_BOTTOM_DIRECTION)){
            return false;
        }

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if ( nextCellDataRightBottom != null && !isAlreadyExistsInList(nextCellDataRightBottom, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataRightBottom, DataGame.RIGHT_BOTTOM_DIRECTION)){
            return false;

        }

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if ( nextCellDataLeftTop != null && !isAlreadyExistsInList(nextCellDataLeftTop, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataLeftTop, DataGame.LEFT_TOP_DIRECTION)){
            return false;

        }

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if ( nextCellDataRightTop != null && !isAlreadyExistsInList(nextCellDataRightTop, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataRightTop, DataGame.RIGHT_TOP_DIRECTION)){
            return false;

        }

        return true;

    }

    public boolean isEqualPlayerCells(CellDataImpl currCellData){

        boolean isPlayerOneCurrently = currCellData.isPlayerOneCurrently();
        return isPlayerOneCurrently  && dataGame.isPlayerOneTurn() || !isPlayerOneCurrently && ! dataGame.isPlayerOneTurn();

    }

    public boolean isAttackMove(CellDataImpl currCellData) {

        if (isQueenPawn(currCellData)){
            return isAttackMoveByQueen(currCellData);
        }

        //Check the left direction
        CellDataImpl nextCellLeft = dataGame.getNextCell(currCellData, true);
        CellDataImpl nextCellChildLeft = dataGame.getNextCell(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = dataGame.getNextCell(currCellData, false);
        CellDataImpl nextCellChildRight = dataGame.getNextCell(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
            return true;
        }

        return false;

    }

    /**
     * Check the the two cells after the empty cell
     *
     * @param currCellData is the full cell after the empty cell
     * @param direction
     * @return
     */
    public boolean isAttackMoveByQueenByDirection(CellDataImpl currCellData, int direction){
        CellDataImpl nextCell = dataGame.getNextCellByDirection(currCellData, direction);
        if (currCellData != null && nextCell != null){
            if (!currCellData.isEmpty() && !isEqualPlayerCells(currCellData) && nextCell.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isAttackMoveByQueen(CellDataImpl currCellData){

        return isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftBottom(), DataGame.LEFT_BOTTOM_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftTop(), DataGame.LEFT_TOP_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightBottom(), DataGame.RIGHT_BOTTOM_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightTop(), DataGame.RIGHT_TOP_DIRECTION);

    }

    public boolean isAttackMoveByDirection(CellDataImpl currCellData, boolean isLeft) {

        if (isQueenPawn(currCellData)){

           return isAttackMoveByQueen(currCellData);

        }

        //Check the right direction
        CellDataImpl nextCell = dataGame.getNextCell(currCellData, isLeft);
        CellDataImpl nextCellChild = dataGame.getNextCell(nextCell, isLeft);

        // 2. check if there is attack turn
        if ((nextCell != null && !nextCell.isEmpty() && !isEqualPlayerCells(nextCell) && nextCellChild != null && nextCellChild.isEmpty())){
            return true;
        }

        return false;
    }

    public boolean isQueenPawn(Point currPawnPoint) {
        return dataGame.getPawnByPoint(currPawnPoint).isMasterPawn();
    }


    public boolean isQueenPawn(CellDataImpl currCellData){
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(currCellData.getPointStartPawn());
        return pawnByPoint != null && pawnByPoint.isMasterPawn();
    }

    public boolean isSomePlayerWin() {

        boolean isPlayerOneCanStart = FluentIterable.from(dataGame.getPawnsPlayerOne().values())
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(this::isCanCellStart)
                .transform(CellDataImpl::getPoint)
                .first()
                .isPresent();

        boolean isPlayerTwoCanStart = FluentIterable.from(dataGame.getPawnsPlayerTwo().values())
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(this::isCanCellStart)
                .transform(CellDataImpl::getPoint)
                .first()
                .isPresent();

        if (dataGame.isPlayerOneTurn()){
            return !isPlayerOneCanStart || dataGame.getPawnsPlayerOne().size() == 0;
        } else {
            return !isPlayerTwoCanStart || dataGame.getPawnsPlayerTwo().size() == 0;
        }

    }
}
