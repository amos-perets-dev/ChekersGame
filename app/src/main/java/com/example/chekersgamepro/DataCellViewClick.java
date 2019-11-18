package com.example.chekersgamepro;

import android.graphics.Color;
import android.graphics.Point;

public class DataCellViewClick {

    private boolean isClickValid;
    private boolean isParent;
    private int color;
    private Point point;
    private boolean isEmpty;

    public DataCellViewClick(boolean isClickValid, Point point, boolean isParent, boolean isEmpty) {
        this.isClickValid = isClickValid;
        this.point = point;
        this.isParent = isParent;
        this.isEmpty = isEmpty;
    }

    public boolean isClickValid() {
        return isClickValid;
    }

    public DataCellViewClick setClickValid(boolean clickValid) {
        isClickValid = clickValid;
        return this;
    }

    public int getColor() {
        return isClickValid
                ? isEmpty || isParent ? Color.GREEN : Color.BLUE
                : Color.RED;
    }

    public DataCellViewClick setColor(int color) {
        this.color = color;
        return this;
    }

    public Point getPoint() {
        return point;
    }

    public DataCellViewClick setPoint(Point point) {
        this.point = point;
        return this;
    }

    public boolean isParent() {
        return isParent;
    }

    public DataCellViewClick setParent(boolean parent) {
        isParent = parent;
        return this;
    }
}
