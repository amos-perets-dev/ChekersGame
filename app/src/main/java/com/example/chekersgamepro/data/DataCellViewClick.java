package com.example.chekersgamepro.data;

import android.graphics.Point;

public class DataCellViewClick {

    private int colorChecked;
    private int colorClearChecked;

    private Point point;

    public DataCellViewClick(Point point, int colorChecked, int colorClearChecked) {
        this.point = point;
        this.colorChecked = colorChecked;
        this.colorClearChecked = colorClearChecked;
    }


    public int getColorChecked() {
        return colorChecked;
    }

    public DataCellViewClick setColor(int colorChecked) {
        this.colorChecked = colorChecked;
        return this;
    }

    public Point getPoint() {
        return point;
    }

    public DataCellViewClick setPoint(Point point) {
        this.point = point;
        return this;
    }

    public int getColorClearChecked() {
        return colorClearChecked;
    }
}
