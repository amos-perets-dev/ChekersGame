package com.example.chekersgamepro.data.move;

import android.graphics.Point;

import java.util.concurrent.ThreadLocalRandom;

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
    private int idCell;

    public Move(Point pointCellStart, Point endPoint, int idCell) {
        this.startPoint = pointCellStart;
        this.endPoint = endPoint;
        this.idCell = idCell;

    }

    public int getIdCell() {
        return idCell;
    }

    public Move() {
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

    public Move setStartPoint(Point endPoint) {
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
    private Long computerTime;

    public Move setComputerTime() {
        computerTime = getComputerTime(ThreadLocalRandom.current().nextInt(1, 3));
        return this;
    }

    public Long getComputerTime() {
        return computerTime;
    }

    private Long getComputerTime(int ComputerTimeType) {
        switch (ComputerTimeType){
            case 1: return 2000L;
            case 2: return 4000L;
            case 3: return 6000L;
            default: return 1000L;
        }
    }
}
