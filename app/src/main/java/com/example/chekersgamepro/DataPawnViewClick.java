package com.example.chekersgamepro;

import android.graphics.Point;

public class DataPawnViewClick {

    private Point point;

    /**
     * Check if this point that the pawn can go to it
     * and if is not valid that means the container cell is not empty and the pawn that in this cell is of the other player that need to be killed
     */
    private boolean isValidPoint;

    public DataPawnViewClick(Point point, boolean isValidPoint) {
        this.point = point;
        this.isValidPoint = isValidPoint;
    }

    public Point getPoint() {
        return point;
    }

    public DataPawnViewClick setPoint(Point point) {
        this.point = point;
        return this;
    }

    public boolean isValidPoint() {
        return isValidPoint;
    }

    public DataPawnViewClick setValidPoint(boolean validPoint) {
        isValidPoint = validPoint;
        return this;
    }
}
