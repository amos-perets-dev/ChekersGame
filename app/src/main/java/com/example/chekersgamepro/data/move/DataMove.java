package com.example.chekersgamepro.data.move;

import com.example.chekersgamepro.data.cell.CellDataImpl;

import java.util.ArrayList;
import java.util.List;

public class DataMove {

    private Move move;

    private CellDataImpl cellDataSrc;

    private CellDataImpl cellDataDst;

    private List<CellDataImpl> cellsListNeedRemove;

    public DataMove(Move move, CellDataImpl cellDataSrc, CellDataImpl cellDataDst, List<CellDataImpl> cellsListNeedRemove) {
        this.move = move;
        this.cellDataSrc = new CellDataImpl(cellDataSrc);
        this.cellDataDst = new CellDataImpl(cellDataDst);
        this.cellsListNeedRemove = new ArrayList<>(cellsListNeedRemove);
    }

    public Move getMove() {
        return move;
    }

    public DataMove setMove(Move move) {
        this.move = move;
        return this;
    }

    public CellDataImpl getCellDataSrc() {
        return cellDataSrc;
    }

    public DataMove setCellDataSrc(CellDataImpl cellDataSrc) {
        this.cellDataSrc = cellDataSrc;
        return this;
    }

    public CellDataImpl getCellDataDst() {
        return cellDataDst;
    }

    public DataMove setCellDataDst(CellDataImpl cellDataDst) {
        this.cellDataDst = cellDataDst;
        return this;
    }

    public List<CellDataImpl> getCellsListNeedRemove() {
        return cellsListNeedRemove;
    }

    public DataMove setCellsListNeedRemove(List<CellDataImpl> cellsListNeedRemove) {
        this.cellsListNeedRemove = cellsListNeedRemove;
        return this;
    }
}
