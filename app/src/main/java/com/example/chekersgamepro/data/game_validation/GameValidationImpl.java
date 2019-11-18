package com.example.chekersgamepro.data.game_validation;

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

        CellDataImpl nextCellLeft = getNextCell(currCellData, true);
        CellDataImpl nextCellChildLeft = getNextCell(nextCellLeft, true);

        CellDataImpl nextCellRight = getNextCell(currCellData, false);
        CellDataImpl nextCellChildRight = getNextCell(nextCellRight, false);

        // 1. check if there is normal turn
        // 2. check if there is attack turn
        if ((nextCellLeft != null && nextCellLeft.isEmpty())
                ||  (nextCellLeft != null && !nextCellLeft.isEmpty() && !isEqualPlayerCells(currCellData, nextCellLeft) && nextCellChildLeft != null && nextCellChildLeft.isEmpty())){
            return true;
        }

        // 1. check if there is normal turn
        // 2. check if there is attack turn
        if ((nextCellRight != null && nextCellRight.isEmpty())
                ||  (nextCellRight != null && !nextCellRight.isEmpty() && !isEqualPlayerCells(currCellData, nextCellRight) && nextCellChildRight != null && nextCellChildRight.isEmpty())){
            return true;
        }

       return false;
    }


    public boolean isLeaf (CellDataImpl currCell){

        if (!currCell.isEmpty()){
            return false;
        }

        CellDataImpl nextCellLeft = getNextCell(currCell, true);
        CellDataImpl nextCellLeftByNextCell = getNextCell(nextCellLeft, true);

        CellDataImpl nextCellRight = getNextCell(currCell, false);
        CellDataImpl nextCellRightByNextCell = getNextCell(nextCellRight, false);

        // check if there is attack path
        if (nextCellLeft != null && nextCellLeftByNextCell != null){
            if (!nextCellLeft.isEmpty() && !isEqualPlayerCells(currCell, nextCellLeft) && nextCellLeftByNextCell.isEmpty()){
                return false;
            }
        }

        // check if there is attack path
        if (nextCellRight != null && nextCellRightByNextCell != null){
            if (!nextCellRight.isEmpty() && !isEqualPlayerCells(currCell, nextCellRight) && nextCellRightByNextCell.isEmpty()){
                return false;
            }
        }

        return true;

    }

    public boolean isEqualPlayerCells(CellDataImpl currCellData){

        boolean isPlayerOneCurrently = currCellData.isPlayerOneCurrently();
        return (isPlayerOneCurrently  && isPlayerOneTurn)
                || (!isPlayerOneCurrently && !isPlayerOneTurn);

    }

    public boolean isEqualPlayerCells(CellDataImpl currCellData, CellDataImpl nextCellData){

        boolean isPlayerOneCurrently = currCellData.isPlayerOneCurrently();
        return (isPlayerOneCurrently  && isPlayerOneTurn)
                || (!isPlayerOneCurrently && !isPlayerOneTurn);

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
