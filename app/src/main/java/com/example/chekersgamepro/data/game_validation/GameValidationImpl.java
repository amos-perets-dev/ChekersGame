package com.example.chekersgamepro.data.game_validation;

import android.util.Log;

import com.example.chekersgamepro.DataGame;
import com.example.chekersgamepro.GameManager;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.List;

import javax.annotation.Nullable;

public class GameValidationImpl implements GameManager.ChangePlayerListener {

    private boolean isPlayerOneTurn;


    private DataGame dataGame = DataGame.getInstance();

    public GameValidationImpl( boolean isPlayerOneTurn
            , List<GameManager.ChangePlayerListener> changePlayerListListeners) {
        this.isPlayerOneTurn = isPlayerOneTurn;
        changePlayerListListeners.add(this);
    }

    private boolean isCanCellStartByDirection(CellDataImpl currCellData, boolean isLeft){
        CellDataImpl nextCell = getNextCell(currCellData, isLeft);
        // 1. check if there is normal turn
        if (nextCell != null && nextCell.isEmpty()){
            return true;
        }

        CellDataImpl nextCellChild = getNextCell(nextCell, isLeft);

        // 2. check if there is attack turn
        if ((nextCell != null && !nextCell.isEmpty() && !isEqualPlayerCells(nextCell) && nextCellChild != null && nextCellChild.isEmpty())){
            return true;
        }

        return false;
    }

    public boolean isCanCellStart(CellDataImpl currCellData){
        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(currCellData.getPointStartPawn());
        if (pawnByPoint != null){
            if (pawnByPoint.isMasterPawn()){
                return isCanCellStartMasterCell(currCellData);
            }
        }

        //Check the left direction<<<
       // boolean isCanCellStartLeftDirection = isCanCellStart(currCellData, true);
//        CellDataImpl nextCellLeft = getNextCell(currCellData, true);
//        // 1. check if there is normal turn
//        if (nextCellLeft != null && nextCellLeft.isEmpty()){
//            return true;
//        }
//
//        CellDataImpl nextCellChildLeft = getNextCell(nextCellLeft, true);
//
//        // 2. check if there is attack turn
//        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
//            return true;
//        }
        //Check the left direction>>>

        //Check the right direction<<<
       // boolean isCanCellStartRightDirection = isCanCellStart(currCellData, false);

//        CellDataImpl nextCellRight = getNextCell(currCellData, false);
//        // 1. check if there is normal turn
//        if (nextCellRight != null && nextCellRight.isEmpty()){
//            return true;
//        }
//
//        CellDataImpl nextCellChildRight = getNextCell(nextCellRight, false);
//
//        // 2. check if there is attack turn
//        if ((nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
//            return true;
//        }
        //Check the right direction>>>

        return isCanCellStartByDirection(currCellData, false) || isCanCellStartByDirection(currCellData, true);
    }

    public boolean isCanCellStartMasterCell(CellDataImpl currCellData){

        CellDataImpl nextCellDataLeftPlayerOne = currCellData.getNextCellDataLeftPlayerOne();
        CellDataImpl nextCellByNextLeftOne = getNextCellByKing(nextCellDataLeftPlayerOne, true);
        if (nextCellDataLeftPlayerOne != null && nextCellDataLeftPlayerOne.isEmpty()
                || nextCellDataLeftPlayerOne != null && !nextCellDataLeftPlayerOne.isEmpty() && !isEqualPlayerCells(nextCellDataLeftPlayerOne) && nextCellByNextLeftOne != null && nextCellByNextLeftOne.isEmpty()) {
            return true;
        }

        CellDataImpl nextCellDataRightPlayerOne = currCellData.getNextCellDataRightPlayerOne();
        CellDataImpl nextCellByNextRightOne = getNextCellByKing(nextCellDataRightPlayerOne, false);
        if (nextCellDataRightPlayerOne != null && nextCellDataRightPlayerOne.isEmpty()
                || nextCellDataRightPlayerOne != null && !nextCellDataRightPlayerOne.isEmpty() && !isEqualPlayerCells(nextCellDataRightPlayerOne) && nextCellByNextRightOne != null && nextCellByNextRightOne.isEmpty()) {
            return true;
        }

        CellDataImpl nextCellDataLeftPlayerTwo = currCellData.getNextCellDataLeftPlayerTwo();
        CellDataImpl nextCellByNextLeftTwo = getNextCellByKing(nextCellDataLeftPlayerTwo, true);
        if (nextCellDataLeftPlayerTwo != null && nextCellDataLeftPlayerTwo.isEmpty()
                || nextCellDataLeftPlayerTwo != null && !nextCellDataLeftPlayerTwo.isEmpty() && !isEqualPlayerCells(nextCellDataLeftPlayerTwo) && nextCellByNextLeftTwo != null && nextCellByNextLeftTwo.isEmpty()) {
            return true;
        }

        CellDataImpl nextCellDataRightPlayerTwo = currCellData.getNextCellDataRightPlayerTwo();
        CellDataImpl nextCellByNextRightTwo = getNextCellByKing(nextCellDataRightPlayerTwo, false);
        if (nextCellDataRightPlayerTwo != null && nextCellDataRightPlayerTwo.isEmpty()
                || nextCellDataRightPlayerTwo != null && !nextCellDataRightPlayerTwo.isEmpty() && !isEqualPlayerCells(nextCellDataRightPlayerTwo) && nextCellByNextRightTwo != null && nextCellByNextRightTwo.isEmpty()) {
            return true;
        }

        return false;

    }


    public boolean isLeaf(CellDataImpl currCell, boolean isMasterPawn){

        if (!currCell.isEmpty()){
            return false;
        }

        CellDataImpl nextCellLeft = isMasterPawn ? getNextCellByKing(currCell, true): getNextCell(currCell, true);

        // check if there is attack path
        if (nextCellLeft != null){
            CellDataImpl nextCellLeftByNextCell = isMasterPawn ? getNextCellByKing(nextCellLeft, true): getNextCell(nextCellLeft, true);
            if (nextCellLeftByNextCell != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellLeftByNextCell.isEmpty()){
                return false;
            }
        }

        CellDataImpl nextCellRight = isMasterPawn ? getNextCellByKing(currCell, false): getNextCell(currCell, false);

        // check if there is attack path
        if (nextCellRight != null){
            CellDataImpl nextCellRightByNextCell = isMasterPawn ? getNextCellByKing(nextCellRight, false): getNextCell(nextCellRight, false);
            if (nextCellRightByNextCell != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellRightByNextCell.isEmpty()){
                return false;
            }
        }

        return true;

    }

    public boolean isEqualPlayerCells(CellDataImpl currCellData){

        boolean isPlayerOneCurrently = currCellData.isPlayerOneCurrently();
        return isPlayerOneCurrently  && isPlayerOneTurn || !isPlayerOneCurrently && !isPlayerOneTurn;

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

//    private CellDataImpl getNextCellByKing(CellDataImpl cellData, boolean isLeft){
//
//        if (cellData != null){
//            if (isLeft){
//                if (isPlayerOneTurn){
//                    if (cellData.getNextCellDataLeftPlayerTwo() != null){
//                        return cellData.getNextCellDataLeftPlayerTwo();
//                    }
//
//                    if (cellData.getNextCellDataLeftPlayerOne() != null){
//                        return cellData.getNextCellDataLeftPlayerOne();
//                    }
//                } else {
//                    if (cellData.getNextCellDataLeftPlayerOne() != null){
//                        return cellData.getNextCellDataLeftPlayerOne();
//                    }
//
//                    if (cellData.getNextCellDataLeftPlayerTwo() != null){
//                        return cellData.getNextCellDataLeftPlayerTwo();
//                    }
//                }
//
//            } else {
//                if (isPlayerOneTurn){
//                    if (cellData.getNextCellDataRightPlayerTwo() != null){
//                        return cellData.getNextCellDataRightPlayerTwo();
//                    }
//
//                    if (cellData.getNextCellDataRightPlayerOne() != null){
//                        return cellData.getNextCellDataRightPlayerOne();
//                    }
//                } else {
//                    if (cellData.getNextCellDataRightPlayerOne() != null){
//                        return cellData.getNextCellDataRightPlayerOne();
//                    }
//
//                    if (cellData.getNextCellDataRightPlayerTwo() != null){
//                        return cellData.getNextCellDataRightPlayerTwo();
//                    }
//                }
//            }
//        }
//
//
//        return null;
//
//    }

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

    @Override
    public void onChangePlayer(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn;
    }

    public boolean isAttackMove(CellDataImpl currCellData/*, boolean isMasterPawn*/) {

        //Check the left direction
        CellDataImpl nextCellLeft = getNextCell(currCellData, true);
        CellDataImpl nextCellChildLeft = getNextCell(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = getNextCell(currCellData, false);
        CellDataImpl nextCellChildRight = getNextCell(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
            return true;
        }

        return false;

    }

    public boolean isAttackMoveByDirection(CellDataImpl currCellData, boolean isLeft, boolean isMasterPawn) {
        //Check the right direction
        CellDataImpl nextCell = isMasterPawn ? getNextCellByKing(currCellData, isLeft) : getNextCell(currCellData, isLeft);
        CellDataImpl nextCellChild = isMasterPawn ? getNextCellByKing(nextCell, isLeft) : getNextCell(nextCell, isLeft);

        // 2. check if there is attack turn
        if ((nextCell != null && !nextCell.isEmpty() && !isEqualPlayerCells(nextCell) && nextCellChild != null && nextCellChild.isEmpty())){
            return true;
        }

        return false;
    }

    /* public boolean isAttackMove(CellDataImpl currCellData) {
        boolean isMasterPawn = false;

        PawnDataImpl pawnByPoint = dataGame.getPawnByPoint(currCellData.getPointStartPawn());
        if (pawnByPoint != null){
            if (pawnByPoint.isMasterPawn()){
                isMasterPawn = true;
            }
        }


        //Check the left direction
        CellDataImpl nextCellLeft = isMasterPawn ? getNextCellByKing(currCellData, true): getNextCell(currCellData, true);
        CellDataImpl nextCellChildLeft = isMasterPawn ? getNextCellByKing(nextCellLeft, true): getNextCell(nextCellLeft, true);

        // 2. check if there is attack turn
        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = isMasterPawn ? getNextCellByKing(currCellData, false): getNextCell(currCellData, false);
        CellDataImpl nextCellChildRight = isMasterPawn ? getNextCellByKing(nextCellRight, false): getNextCell(nextCellRight, false);

        // 2. check if there is attack turn
        if ((nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
            return true;
        }

        return false;

    }

    public boolean isAttackMoveByDirection(CellDataImpl currCellData, boolean isLeft, boolean isMasterPawn) {
        //Check the right direction
        CellDataImpl nextCell = isMasterPawn ? getNextCellByKing(currCellData, isLeft): getNextCell(currCellData, isLeft); getNextCell(currCellData, isLeft);
        CellDataImpl nextCellChild = isMasterPawn ? getNextCellByKing(currCellData, isLeft): getNextCell(currCellData, isLeft); getNextCell(currCellData, isLeft);

        // 2. check if there is attack turn
        if ((nextCell != null && !nextCell.isEmpty() && !isEqualPlayerCells(nextCell) && nextCellChild != null && nextCellChild.isEmpty())){
            return true;
        }

        return false;
    }*/


}
