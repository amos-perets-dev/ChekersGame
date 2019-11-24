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

        if ((currCellData.getPoint().x < prevCellData.getPoint().x) && (currCellData.getPoint().y < prevCellData.getPoint().y)) {
            Log.d("TEST_GAME", "getNextCellDataLeftTop: ");
            return currCellData.getNextCellDataLeftTop();
        }

        if ((currCellData.getPoint().x < prevCellData.getPoint().x) && (currCellData.getPoint().y > prevCellData.getPoint().y)) {
            Log.d("TEST_GAME", "getNextCellDataLeftBottom: ");

            return currCellData.getNextCellDataLeftBottom();

        }

        if ((currCellData.getPoint().x > prevCellData.getPoint().x) && (currCellData.getPoint().y < prevCellData.getPoint().y)) {
            Log.d("TEST_GAME", "getNextCellDataRightTop: ");

            return currCellData.getNextCellDataRightTop();

        }

        if ((currCellData.getPoint().x > prevCellData.getPoint().x) && (currCellData.getPoint().y > prevCellData.getPoint().y)) {
            Log.d("TEST_GAME", "getNextCellDataRightBottom: ");

            return currCellData.getNextCellDataRightBottom();

        }

        return null;
    }

    public CellDataImpl getNextCellByDirection(CellDataImpl currCellData, int direction) {
        if (currCellData == null) return null;

        if (direction == DataGame.LEFT_TOP_DIRECTION ) {
            return currCellData.getNextCellDataLeftTop();
        }

        if (direction == DataGame.LEFT_BOTTOM_DIRECTION) {
            return currCellData.getNextCellDataLeftBottom();
        }

        if (direction == DataGame.RIGHT_TOP_DIRECTION) {
            return currCellData.getNextCellDataRightTop();
        }

        if (direction == DataGame.RIGHT_BOTTOM_DIRECTION) {
            return currCellData.getNextCellDataRightBottom();
        }

        return null;
    }

}
