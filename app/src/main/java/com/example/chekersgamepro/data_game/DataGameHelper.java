package com.example.chekersgamepro.data_game;

import android.util.Log;

import com.example.chekersgamepro.data.cell.CellDataImpl;

public class DataGameHelper {

    public CellDataImpl getNextCell(CellDataImpl cellData, boolean isLeft, boolean isPlayerOneTurn) {
        return cellData != null
                ? isPlayerOneTurn
                ? isLeft
                ? cellData.getNextCellDataLeftBottom()
                : cellData.getNextCellDataRightBottom()
                : isLeft
                ? cellData.getNextCellDataLeftTop()
                : cellData.getNextCellDataRightTop()
                : null;

    }

    public CellDataImpl getNextCell(CellDataImpl currCellData, CellDataImpl prevCellData) {
        if (currCellData == null) return null;

        if ((currCellData.getPointCell().x < prevCellData.getPointCell().x) && (currCellData.getPointCell().y < prevCellData.getPointCell().y)) {
            return currCellData.getNextCellDataLeftTop();
        }

        if ((currCellData.getPointCell().x < prevCellData.getPointCell().x) && (currCellData.getPointCell().y > prevCellData.getPointCell().y)) {
            return currCellData.getNextCellDataLeftBottom();

        }

        if ((currCellData.getPointCell().x > prevCellData.getPointCell().x) && (currCellData.getPointCell().y < prevCellData.getPointCell().y)) {
            return currCellData.getNextCellDataRightTop();

        }

        if ((currCellData.getPointCell().x > prevCellData.getPointCell().x) && (currCellData.getPointCell().y > prevCellData.getPointCell().y)) {
            return currCellData.getNextCellDataRightBottom();

        }

        return null;
    }

    public CellDataImpl getNextCellByDirection(CellDataImpl currCellData, int direction) {
        if (currCellData == null) return null;

        if (direction == DataGame.Direction.LEFT_TOP_DIRECTION ) {
            return currCellData.getNextCellDataLeftTop();
        }

        if (direction == DataGame.Direction.LEFT_BOTTOM_DIRECTION) {
            return currCellData.getNextCellDataLeftBottom();
        }

        if (direction == DataGame.Direction.RIGHT_TOP_DIRECTION) {
            return currCellData.getNextCellDataRightTop();
        }

        if (direction == DataGame.Direction.RIGHT_BOTTOM_DIRECTION) {
            return currCellData.getNextCellDataRightBottom();
        }

        return null;
    }

}
