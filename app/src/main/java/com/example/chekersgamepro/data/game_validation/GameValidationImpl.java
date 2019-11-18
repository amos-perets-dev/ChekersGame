package com.example.chekersgamepro.data.game_validation;

import android.util.Log;

import com.example.chekersgamepro.GameManager;
import com.example.chekersgamepro.data.cell.CellDataImpl;

import java.util.List;

import javax.annotation.Nullable;

public class GameValidationImpl implements GameManager.ChangePlayerListener {

    private boolean isPlayerOneTurn;

    public GameValidationImpl( boolean isPlayerOneTurn
            , List<GameManager.ChangePlayerListener> changePlayerListListeners) {
        this.isPlayerOneTurn = isPlayerOneTurn;
        changePlayerListListeners.add(this);
    }

    public boolean isCanCellStart(CellDataImpl currCellData){

        //Check the left direction
        CellDataImpl nextCellLeft = getNextCell(currCellData, true);

        if (nextCellLeft != null && nextCellLeft.isEmpty()){
            return true;
        }

        CellDataImpl nextCellChildLeft = getNextCell(nextCellLeft, true);

        // 1. check if there is normal turn
        // 2. check if there is attack turn
        if ((nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        //Check the right direction
        CellDataImpl nextCellRight = getNextCell(currCellData, false);

        if (nextCellRight != null && nextCellRight.isEmpty()){
            return true;
        }

        CellDataImpl nextCellChildRight = getNextCell(nextCellLeft, false);

        // 1. check if there is normal turn
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

        CellDataImpl nextCellLeft = getNextCell(currCell, true);

        // check if there is attack path
        if (nextCellLeft != null){
            CellDataImpl nextCellLeftByNextCell = getNextCell(nextCellLeft, true);
            if (nextCellLeftByNextCell != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(nextCellLeft) && nextCellLeftByNextCell.isEmpty()){
                return false;
            }
        }

        CellDataImpl nextCellRight = getNextCell(currCell, false);

        // check if there is attack path
        if (nextCellRight != null){
            CellDataImpl nextCellRightByNextCell = getNextCell(nextCellRight, false);
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

    @Override
    public void onChangePlayer(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn;
    }

}
