package com.example.chekersgamepro.data.move;

import android.graphics.Point;

/**
 * Creates a new move
 * the current row of the piece
 * the current column of the piece
 * the row the piece will be moved to
 * the column the piece will be moved to
 */
public class Move {

    private Point startPoint;
    private Point endPoint;

    public Move(Point pointCellStart, Point endPoint) {
        this.startPoint = pointCellStart;
        this.endPoint = endPoint;
    }

    public Point getStartPoint() {
        return startPoint;
    }


    public Point getEndPoint() {
        return endPoint;
    }

    public Move setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    /**
     * Creates a string representation of a move
     * @return a string version of a move
     */
    public String toString() {
        return "startPoint: (" + startPoint + ", endPoint: " + endPoint + ")";
    }
}
