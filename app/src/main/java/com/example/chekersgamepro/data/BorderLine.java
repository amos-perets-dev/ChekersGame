package com.example.chekersgamepro.data;

import android.graphics.Point;

public class BorderLine {

    private Point start;
    private Point end;

    public BorderLine() {
    }

    public BorderLine(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }

    public BorderLine setStart(Point start) {
        this.start = start;
        return this;
    }

    public Point getEnd() {
        return end;
    }

    public BorderLine setEnd(Point end) {
        this.end = end;
        return this;
    }
}
