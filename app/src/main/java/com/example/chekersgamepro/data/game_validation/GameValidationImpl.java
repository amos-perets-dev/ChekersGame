package com.example.chekersgamepro.data.game_validation;

import android.graphics.Point;

import com.example.chekersgamepro.DataGame;
import com.example.chekersgamepro.GameManager;
import com.example.chekersgamepro.data.cell.CellDataImpl;

import java.util.List;

public class GameValidationImpl {

    private DataGame dataGame = DataGame.getInstance();

    public GameValidationImpl() {

    }


    public boolean isCanCellStart(CellDataImpl currCellData){

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


    public boolean isLeaf (CellDataImpl currCell){

        if (!currCell.isEmpty()){
            return false;
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

    public boolean isEqualPlayerCells(CellDataImpl currCellData){

        boolean isPlayerOneCurrently = currCellData.isPlayerOneCurrently();
        return isPlayerOneCurrently  && dataGame.isPlayerOneTurn() || !isPlayerOneCurrently && ! dataGame.isPlayerOneTurn();

    }

    public boolean isAttackMove(CellDataImpl currCellData) {

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

    public boolean isAttackMoveByDirection(CellDataImpl currCellData, boolean isLeft) {
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
}
