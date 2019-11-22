package com.example.chekersgamepro.data.cell;

import android.graphics.Point;

import javax.annotation.Nullable;

public class CellDataImpl {

    public static final int PLAYER_ONE = 1;
    public static final int PLAYER_TWO = 2;
    public static final int EMPTY = 0;

    private Point point;
    private Point pointStartPawn;

    private int width;
    private int height;
    private int color;

    private float alphaCell;

    private boolean isDarkCell;
    private boolean isParent;
    private boolean isValidCell;
    private boolean isEmpty;
    private boolean isEmptyFirstTimeDraw;
    private boolean isMasterCell;
    private boolean isLeaf = false;
    private boolean isNode = false;
    private boolean isRightDirectionValid = false;
    private boolean isLeftDirectionValid = false;
    private boolean isPlayerOneCurrently;

    private @Nullable CellDataImpl nextCellDataLeftPlayerOne;
    private @Nullable CellDataImpl nextCellDataRightPlayerOne;

    private @Nullable CellDataImpl nextCellDataLeftPlayerTwo;
    private @Nullable CellDataImpl nextCellDataRightPlayerTwo;

    public CellDataImpl(boolean isValidCell
            , boolean isEmpty
            , boolean isDarkCell
            , Point point
            , boolean isEmptyFirstTimeDraw
            , boolean isMasterCell
            , int width
            , int height
            , boolean isPlayerOneCurrently
            , Point pointStartPawn) {

        this.isValidCell = isValidCell;
        this.isEmpty = isEmpty;
        this.isDarkCell = isDarkCell;
        this.point = point;
        this.isEmptyFirstTimeDraw = isEmptyFirstTimeDraw;
        this.isMasterCell = isMasterCell;
        this.width = width;
        this.height = height;
        this.isPlayerOneCurrently = isPlayerOneCurrently;
        this.pointStartPawn = pointStartPawn;

    }

    public Point getPointStartPawn() {
        return pointStartPawn;
    }

    public CellDataImpl setPointStartPawn(Point pointStartPawn) {
        this.pointStartPawn = pointStartPawn;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public CellDataImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public CellDataImpl setHeight(int height) {
        this.height = height;
        return this;
    }

    public CellDataImpl setDarkCell(boolean darkCell) {
        isDarkCell = darkCell;
        return this;
    }

    public boolean isDarkCell() {
        return isDarkCell;
    }

    public CellDataImpl setisDarkCell(boolean isDarkCell) {
        this.isDarkCell = isDarkCell;
        return this;
    }

    public Point getPoint() {
        return point;
    }

    public CellDataImpl setPoint(Point point) {
        this.point = point;
        return this;
    }

    public boolean isValidCell() {
        return isValidCell;
    }

    public CellDataImpl setValidCell(boolean validCell) {
        isValidCell = validCell;
        return this;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public CellDataImpl setEmpty(boolean empty) {
        isEmpty = empty;
        return this;
    }

    public boolean isEmptyFirstTimeDraw() {
        return isEmptyFirstTimeDraw;
    }

    public CellDataImpl setEmptyFirstTimeDraw(boolean emptyFirstTimeDraw) {
        isEmptyFirstTimeDraw = emptyFirstTimeDraw;
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
        return isDarkCell ? 1 : 0.35f;
    }

    public CellDataImpl setAlphaCell(float alphaCell) {
        this.alphaCell = alphaCell;
        return this;
    }

    public boolean isPlayerOneCurrently() {
        return isPlayerOneCurrently;
    }


    public CellDataImpl setPlayerOneCurrently(boolean isPlayerOneCurrently) {
        this.isPlayerOneCurrently = isPlayerOneCurrently;
        return this;
    }

    public CellDataImpl setColor(int color) {
        this.color = color;
        return this;
    }

    public int getColor() {
        return color;
    }

    public CellDataImpl setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
        return this;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Nullable
    public CellDataImpl getNextCellDataLeftBottom() {
        return nextCellDataLeftPlayerOne;
    }

    public CellDataImpl setNextCellDataLeftBottom(CellDataImpl nextCellDataLeftPlayerOne) {
        this.nextCellDataLeftPlayerOne = nextCellDataLeftPlayerOne;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataRightBottom() {
        return nextCellDataRightPlayerOne;
    }

    public CellDataImpl setNextCellDataRightBottom(CellDataImpl nextCellDataLRight) {
        this.nextCellDataRightPlayerOne = nextCellDataLRight;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataLeftTop() {
        return nextCellDataLeftPlayerTwo;
    }

    public CellDataImpl setNextCellDataLeftTop(@Nullable CellDataImpl nextCellDataLeftPlayerTwo) {
        this.nextCellDataLeftPlayerTwo = nextCellDataLeftPlayerTwo;
        return this;
    }

    @Nullable
    public CellDataImpl getNextCellDataRightTop() {
        return nextCellDataRightPlayerTwo;
    }

    public CellDataImpl setNextCellDataRightTop(@Nullable CellDataImpl nextCellDataRightPlayerTwo) {
        this.nextCellDataRightPlayerTwo = nextCellDataRightPlayerTwo;
        return this;
    }

    public boolean isNode() {
        return isNode;
    }

    public CellDataImpl setNode(boolean node) {
        isNode = node;
        return this;
    }

    public boolean isParent() {
        return isParent;
    }

    public CellDataImpl setParent(boolean parent) {
        isParent = parent;
        return this;
    }

    public boolean isRightDirectionValid() {
        return isRightDirectionValid;
    }

    public CellDataImpl setRightDirectionValid(boolean rightDirectionValid) {
        isRightDirectionValid = rightDirectionValid;
        return this;
    }

    public boolean isLeftDirectionValid() {
        return isLeftDirectionValid;
    }

    public CellDataImpl setLeftDirectionValid(boolean leftDirectionValid) {
        isLeftDirectionValid = leftDirectionValid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellDataImpl)) return false;
        CellDataImpl cellData = (CellDataImpl) o;
        return
                isValidCell() == cellData.isValidCell() &&
                        isEmpty() == cellData.isEmpty() &&
                        isMasterCell() == cellData.isMasterCell() &&
                        getPoint().x == cellData.getPoint().x && getPoint().y == cellData.getPoint().y;
    }

}
