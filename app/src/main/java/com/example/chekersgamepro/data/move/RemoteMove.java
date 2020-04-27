package com.example.chekersgamepro.data.move;

import io.realm.RealmObject;

public class RemoteMove extends RealmObject {

    private int idStartCell = -1;
    private int idEndCell = -1;

    public RemoteMove() {

    }

    public int getIdStartCell() {
        return idStartCell;
    }

    public RemoteMove setIdStartCell(int idStartCell) {
        this.idStartCell = idStartCell;
        return this;
    }

    public int getIdEndCell() {
        return idEndCell;
    }

    public RemoteMove setIdEndCell(int idEndCell) {
        this.idEndCell = idEndCell;
        return this;
    }

}
