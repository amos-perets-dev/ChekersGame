package com.example.chekersgamepro.data.pawn;

import android.graphics.Point;

import com.example.chekersgamepro.R;

public class PawnDataImpl {

    private Point startXY;
    private Point containerCellXY;
    private float radius;

    private int idPawn;
    private int color;
    private int width;
    private int height;
    private int icon;

    private boolean isMasterPawn = false;
    private boolean isKilled = false;
    private boolean isPlayerOne;


    public PawnDataImpl(int idPawn, Point containerCellXY, boolean isPlayerOne, Point startXY, int width, int height ) {
        this.idPawn = idPawn;
        this.containerCellXY = containerCellXY;
        this.isPlayerOne = isPlayerOne;
        this.startXY = startXY;
        this.width = width;
        this.height = height;
    }

    public Point getContainerCellXY() {
        return containerCellXY;
    }

    public PawnDataImpl setContainerCellXY(Point containerCellXY) {
        this.containerCellXY = containerCellXY;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public PawnDataImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public PawnDataImpl setHeight(int height) {
        this.height = height;
        return this;
    }

    public Point getStartXY() {
        return startXY;
    }

    public PawnDataImpl setStartXY(Point startXY) {
        this.startXY = startXY;
        return this;
    }

    public boolean isPlayerOne() {
        return isPlayerOne;
    }

    public boolean isKilled() {
        return isKilled;
    }

    public void setKilled(boolean killed) {
        isKilled = killed;
    }

    public int getColor() {
        return color;
    }

    public PawnDataImpl setColor(int color) {
        this.color = color;
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public PawnDataImpl setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public int getIdPawn() {
        return idPawn;
    }

    public PawnDataImpl setIdPawn(int idPawn) {
        this.idPawn = idPawn;
        return this;
    }

    public boolean isMasterPawn() {
        return isMasterPawn;
    }

    public PawnDataImpl setMasterPawn(boolean masterPawn) {
        this.isMasterPawn = masterPawn;
        return this;
    }

    public int getIcon() {
        return isPlayerOne ? R.drawable.ic_pawn_one : R.drawable.ic_pawn_two;
    }
}
