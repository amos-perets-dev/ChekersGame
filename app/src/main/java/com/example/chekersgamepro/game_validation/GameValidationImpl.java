package com.example.chekersgamepro.game_validation;

import android.util.Log;

import com.example.chekersgamepro.ai.DataGameBoard;
import com.example.chekersgamepro.data.DataCellViewClick;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;
import com.google.common.collect.FluentIterable;

import java.util.List;

public class GameValidationImpl {

    private DataGame dataGame = DataGame.getInstance();

    private DataGameBoard dataGameBoard;

    public GameValidationImpl(DataGameBoard dataGameBoard) {
        this.dataGameBoard = dataGameBoard;
    }

    private CellDataImpl getNextCellByRelevantData(CellDataImpl currCellData, boolean isLeft){

        return dataGameBoard == null
                ? dataGame.getNextCell(currCellData, isLeft, dataGame.isPlayerOneTurn())
                : dataGameBoard.getNextCell(currCellData, isLeft, dataGameBoard.isPlayerOneCurrently());
    }

    public boolean isEqualPlayersByRelevantData(CellDataImpl currCellData){
        boolean isPlayerOneCurrently = currCellData.getCellContain() == DataGame.CellState.PLAYER_ONE || currCellData.getCellContain() == DataGame.CellState.PLAYER_ONE_KING;

        return dataGameBoard == null
                ? isPlayerOneCurrently  && dataGame.isPlayerOneTurn() || !isPlayerOneCurrently && ! dataGame.isPlayerOneTurn()
                : isPlayerOneCurrently  && dataGameBoard.isPlayerOneCurrently() || !isPlayerOneCurrently && !dataGameBoard.isPlayerOneCurrently();


    }

    private boolean isCanCellStartByQueen(CellDataImpl currCellData){

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null){
            CellDataImpl nextCellByNextLeftBottom = nextCellDataLeftBottom.getNextCellDataLeftBottom();
            if (nextCellDataLeftBottom.getCellContain() == DataGame.CellState.EMPTY
                    || nextCellDataLeftBottom.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellDataLeftBottom)
                    && nextCellByNextLeftBottom != null && nextCellByNextLeftBottom.getCellContain() == DataGame.CellState.EMPTY) {
                return true;
            }
        }

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if (nextCellDataLeftTop != null){
            CellDataImpl nextCellByNextLeftTop = nextCellDataLeftTop.getNextCellDataLeftTop();
            if (nextCellDataLeftTop.getCellContain() == DataGame.CellState.EMPTY
                    || nextCellDataLeftTop.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellDataLeftTop)
                    && nextCellByNextLeftTop != null && nextCellByNextLeftTop.getCellContain() == DataGame.CellState.EMPTY) {
                return true;
            }
        }

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if (nextCellDataRightBottom != null){
            CellDataImpl nextCellByNextRightBottom = nextCellDataRightBottom.getNextCellDataRightBottom();
            if (nextCellDataRightBottom.getCellContain() == DataGame.CellState.EMPTY
                    || nextCellDataRightBottom.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellDataRightBottom)
                    && nextCellByNextRightBottom != null && nextCellByNextRightBottom.getCellContain() == DataGame.CellState.EMPTY) {
                return true;
            }
        }

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if (nextCellDataRightTop != null){
            CellDataImpl nextCellByNextRightTop = nextCellDataRightTop.getNextCellDataRightTop();
            if (nextCellDataRightTop.getCellContain() == DataGame.CellState.EMPTY
                    || nextCellDataRightTop.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellDataRightTop)
                    && nextCellByNextRightTop != null && nextCellByNextRightTop.getCellContain() == DataGame.CellState.EMPTY) {
                return true;
            }
        }

        return false;

    }

    public boolean isAlreadyExists(CellDataImpl currCellData,List<DataCellViewClick> dataOptionalPathByView){
        return FluentIterable.from(dataOptionalPathByView)
                .transform(DataCellViewClick::getPoint)
                .filter(point -> currCellData.getPointCell().x == point.x && currCellData.getPointCell().y == point.y)
                .first()
                .isPresent();
    }

    public boolean isCanCellStart(CellDataImpl currCellData){

        if (isQueenPawn(currCellData)){
            return isCanCellStartByQueen(currCellData);
        }

        //Check the left direction
        CellDataImpl nextCellLeft = getNextCellByRelevantData(currCellData, true);
        // 1. check if there is normal turn
        if (nextCellLeft != null && nextCellLeft.getCellContain() == DataGame.CellState.EMPTY){
            return true;
        }

        CellDataImpl nextCellChildLeft = getNextCellByRelevantData(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && nextCellLeft.getCellContain() != DataGame.CellState.EMPTY
                && !isEqualPlayersByRelevantData(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.getCellContain() == DataGame.CellState.EMPTY)){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = getNextCellByRelevantData(currCellData, false);

        // 1. check if there is normal turn
        if (nextCellRight != null && nextCellRight.getCellContain() == DataGame.CellState.EMPTY){

            return true;
        }

        CellDataImpl nextCellChildRight = getNextCellByRelevantData(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && nextCellRight.getCellContain() != DataGame.CellState.EMPTY
                && !isEqualPlayersByRelevantData(nextCellRight) && nextCellChildRight != null && nextCellChildRight.getCellContain() == DataGame.CellState.EMPTY)){
            return true;
        }

        return false;
    }


    public boolean isLeaf(CellDataImpl currCell, List<DataCellViewClick> dataOptionalPathByView, boolean isQueenPawn){

        if (currCell.getCellContain() != DataGame.CellState.EMPTY){
            return false;
        }

        if (isQueenPawn){
            return isLeafByQueen(currCell, dataOptionalPathByView);
        }

        CellDataImpl nextCellLeft = getNextCellByRelevantData(currCell, true);

        // check if there is attack path
        if (nextCellLeft != null){
            CellDataImpl nextCellLeftByNextCell = getNextCellByRelevantData(nextCellLeft, true);
            if (nextCellLeftByNextCell != null
                    && nextCellLeft.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellLeft) && nextCellLeftByNextCell.getCellContain() == DataGame.CellState.EMPTY){
                return false;
            }
        }

        CellDataImpl nextCellRight = getNextCellByRelevantData(currCell, false);

        // check if there is attack path
        if (nextCellRight != null){
            CellDataImpl nextCellRightByNextCell = getNextCellByRelevantData(nextCellRight, false);
            if (nextCellRightByNextCell != null
                    && nextCellRight.getCellContain() != DataGame.CellState.EMPTY && !isEqualPlayersByRelevantData(nextCellRight) && nextCellRightByNextCell.getCellContain() == DataGame.CellState.EMPTY){
                return false;
            }
        }

        return true;

    }

    private boolean isAlreadyExistsInList(CellDataImpl currCellData, List<DataCellViewClick> dataOptionalPathByView){
        return FluentIterable.from(dataOptionalPathByView)
                .transform(DataCellViewClick::getPoint)
                .filter(point -> currCellData.getPointCell().x == point.x && currCellData.getPointCell().y == point.y)
                .first()
                .isPresent();
    }


    private boolean isLeafByQueen(CellDataImpl currCellData, List<DataCellViewClick> dataOptionalPathByView) {

        CellDataImpl nextCellDataLeftBottom = currCellData.getNextCellDataLeftBottom();
        if (nextCellDataLeftBottom != null
                && !isAlreadyExistsInList(nextCellDataLeftBottom, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataLeftBottom, DataGame.Direction.LEFT_BOTTOM_DIRECTION)){
            return false;
        }

        CellDataImpl nextCellDataRightBottom = currCellData.getNextCellDataRightBottom();
        if ( nextCellDataRightBottom != null
                && !isAlreadyExistsInList(nextCellDataRightBottom, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataRightBottom, DataGame.Direction.RIGHT_BOTTOM_DIRECTION)){
            return false;

        }

        CellDataImpl nextCellDataLeftTop = currCellData.getNextCellDataLeftTop();
        if ( nextCellDataLeftTop != null
                && !isAlreadyExistsInList(nextCellDataLeftTop, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataLeftTop, DataGame.Direction.LEFT_TOP_DIRECTION)){
            return false;

        }

        CellDataImpl nextCellDataRightTop = currCellData.getNextCellDataRightTop();
        if ( nextCellDataRightTop != null
                && !isAlreadyExistsInList(nextCellDataRightTop, dataOptionalPathByView) && isAttackMoveByQueenByDirection(nextCellDataRightTop, DataGame.Direction.RIGHT_TOP_DIRECTION)){
            return false;

        }

        return true;

    }

    public boolean isEqualPlayerCells(CellDataImpl currCellData){

        boolean isPlayerOneCurrently = currCellData.getCellContain() == DataGame.CellState.PLAYER_ONE || currCellData.getCellContain() == DataGame.CellState.PLAYER_ONE_KING;
        return isPlayerOneCurrently  && dataGame.isPlayerOneTurn() || !isPlayerOneCurrently && ! dataGame.isPlayerOneTurn();

    }

    public boolean isMoveAttack(CellDataImpl currCellData) {

        if (isQueenPawn(currCellData)){
            return isAttackMoveByQueen(currCellData);
        }

        //Check the left direction
        CellDataImpl nextCellLeft = getNextCellByRelevantData(currCellData, true);
        CellDataImpl nextCellChildLeft = getNextCellByRelevantData(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && nextCellLeft.getCellContain() != DataGame.CellState.EMPTY
                && !isEqualPlayersByRelevantData(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.getCellContain() == DataGame.CellState.EMPTY)){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = getNextCellByRelevantData(currCellData, false);
        CellDataImpl nextCellChildRight = getNextCellByRelevantData(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && nextCellRight.getCellContain() != DataGame.CellState.EMPTY
                && !isEqualPlayersByRelevantData(nextCellRight) && nextCellChildRight != null && nextCellChildRight.getCellContain() == DataGame.CellState.EMPTY)){
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
            if (currCellData.getCellContain() != DataGame.CellState.EMPTY
                    && !isEqualPlayersByRelevantData(currCellData) && nextCell.getCellContain() == DataGame.CellState.EMPTY) {
                return true;
            }
        }

        return false;
    }

    private boolean isAttackMoveByQueen(CellDataImpl currCellData){

        return isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftBottom(), DataGame.Direction.LEFT_BOTTOM_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataLeftTop(), DataGame.Direction.LEFT_TOP_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightBottom(), DataGame.Direction.RIGHT_BOTTOM_DIRECTION)
                || isAttackMoveByQueenByDirection(currCellData.getNextCellDataRightTop(), DataGame.Direction.RIGHT_TOP_DIRECTION);

    }

    public boolean isAttackMoveByDirection(CellDataImpl currCellData, boolean isLeft) {

        if (isQueenPawn(currCellData)){
            return isAttackMoveByQueen(currCellData);

        }

        //Check the right direction
        CellDataImpl nextCell = getNextCellByRelevantData(currCellData, isLeft);
        CellDataImpl nextCellChild = getNextCellByRelevantData(nextCell, isLeft);

        // 2. check if there is attack turn
        if ((nextCell != null && nextCell.getCellContain() != DataGame.CellState.EMPTY
                && !isEqualPlayersByRelevantData(nextCell) && nextCellChild != null && nextCellChild.getCellContain() == DataGame.CellState.EMPTY)){
            return true;
        }

        return false;
    }

    public boolean isQueenPawn(CellDataImpl currCellData){
        return currCellData.getCellContain() == DataGame.CellState.PLAYER_ONE_KING || currCellData.getCellContain() == DataGame.CellState.PLAYER_TWO_KING;
    }

    public boolean isSomePlayerWin(boolean isPlayerOneTurn) {

        boolean isPlayerOneCanStart = FluentIterable.from(dataGame.getPawnsPlayerOne().values())
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(this::isCanCellStart)
                .transform(CellDataImpl::getPointCell)
                .first()
                .isPresent();

        boolean isPlayerTwoCanStart = FluentIterable.from(dataGame.getPawnsPlayerTwo().values())
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(this::isCanCellStart)
                .transform(CellDataImpl::getPointCell)
                .first()
                .isPresent();

        if (dataGame.isPlayerOneTurn()){
            return !isPlayerOneCanStart || dataGame.getPawnsPlayerOne().size() == 0;
        } else {
            return !isPlayerTwoCanStart || dataGame.getPawnsPlayerTwo().size() == 0;
        }

    }

}
