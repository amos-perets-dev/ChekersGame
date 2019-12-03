package com.example.chekersgamepro.data.cell;

import android.graphics.Point;

import com.example.chekersgamepro.data_game.DataGame;

import javax.annotation.Nullable;

public class CellDataImpl {

    private Point pointCell;
    private Point pointStartPawn;

    private int widthCell;
    private int heightCell;
    private int cellContain = DataGame.CellState.INVALID_STATE;

    private boolean isMasterCell;

    private @Nullable CellDataImpl nextCellDataLeftBottom;
    private @Nullable CellDataImpl nextCellDataRightBottom;

    private @Nullable CellDataImpl nextCellDataLeftTop;
    private @Nullable CellDataImpl nextCellDataRightTop;

    public CellDataImpl(){}

    public CellDataImpl (CellDataImpl cellData) {

        this.pointCell = cellData.getPointCell();
        this.isMasterCell = cellData.isMasterCell();
        this.widthCell = cellData.getWidthCell();
        this.heightCell = cellData.getHeightCell();
        this.pointStartPawn = cellData.getPointStartPawn();
        this.cellContain = cellData.getCellContain();
        this.nextCellDataLeftBottom = cellData.getNextCellDataLeftBottom();
        this.nextCellDataLeftTop = cellData.getNextCellDataLeftTop();
        this.nextCellDataRightBottom = cellData.getNextCellDataRightBottom();
        this.nextCellDataRightTop = cellData.getNextCellDataRightTop();
    }

    public CellDataImpl(int cellContain
            , Point pointCell
            , Point pointStartPawn
            , boolean isMasterCell
            , int widthCell
            , int heightCell) {

        this.cellContain = cellContain;
        this.pointCell = pointCell;
        this.pointStartPawn = pointStartPawn;
        this.isMasterCell = isMasterCell;
        this.widthCell = widthCell;
        this.heightCell = heightCell;

    }

    public int getCellContain() {
        return cellContain;
    }

    public CellDataImpl setCellContain(int cellContain) {
        this.cellContain = cellContain;
        return this;
    }

    public Point getPointStartPawn() {
        return pointStartPawn;
    }

    public CellDataImpl setPointStartPawn(Point pointStartPawn) {
        this.pointStartPawn = pointStartPawn;
        return this;
    }

    public int getWidthCell() {
        return widthCell;
    }

    public CellDataImpl setWidthCell(int widthCell) {
        this.widthCell = widthCell;
        return this;
    }

    public int getHeightCell() {
        return heightCell;
    }

    public CellDataImpl setHeightCell(int heightCell) {
        this.heightCell = heightCell;
        return this;
    }

    public Point getPointCell() {
        return pointCell;
    }

    public CellDataImpl setPointCell(Point pointCell) {
        this.pointCell = pointCell;
        return this;
    }

    public boolean isMasterCell() {
        return isMasterCell;
    }

    public CellDataImpl setMasterCell(boolean masterCell) {
        isMasterCell = masterCell;
        return this;
    }


    public float getAlphaCell() {
        return cellContain == DataGame.CellState.EMPTY_INVALID ? 0.35f : 1;
    }

    @Nullable
    public CellDataImpl getNextCellDataLeftBottom() {
        return nextCellDataLeftBottom;
    }

    public CellDataImpl setNextCellDataLeftBottom(CellDataImpl nextCellDataLeftBottom) {
        this.nextCellDataLeftBottom = nextCellDataLeftBottom;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataRightBottom() {
        return nextCellDataRightBottom;
    }

    public CellDataImpl setNextCellDataRightBottom(CellDataImpl nextCellDataRightBottom) {
        this.nextCellDataRightBottom = nextCellDataRightBottom;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataLeftTop() {
        return nextCellDataLeftTop;
    }

    public CellDataImpl setNextCellDataLeftTop(@Nullable CellDataImpl nextCellDataLeftTop) {
        this.nextCellDataLeftTop = nextCellDataLeftTop;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataRightTop() {
        return nextCellDataRightTop;
    }

    public CellDataImpl setNextCellDataRightTop(@Nullable CellDataImpl nextCellDataRightTop) {
        this.nextCellDataRightTop = nextCellDataRightTop;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellDataImpl)) return false;
        CellDataImpl cellData = (CellDataImpl) o;
        return
                isMasterCell() == cellData.isMasterCell() &&
                        getPointCell().x == cellData.getPointCell().x && getPointCell().y == cellData.getPointCell().y;
    }

    public String print() {
        return "CellDataImpl{" +
                "pointCell=" + pointCell +
                ", cellContain=" + cellContain +
                ", isMasterCell=" + isMasterCell +
                '}';
    }

    @Override
    public String toString() {
        return "CellDataImpl{" +
                "pointCell=" + pointCell +
                ", cellContain=" + cellContain +
                ", isMasterCell=" + isMasterCell +
                ", nextCellDataRightTop=" + (nextCellDataRightTop == null ? "NULL" : "NON NULL")+
                ", nextCellDataLeftTop=" + (nextCellDataLeftTop == null ? "NULL" : "NON NULL")+
                ", nextCellDataRightBottom=" + (nextCellDataRightBottom == null ? "NULL" : "NON NULL")+
                ", nextCellDataLeftBottom=" + (nextCellDataLeftBottom == null ? "NULL" : "NON NULL")+
                '}';
    }
}
