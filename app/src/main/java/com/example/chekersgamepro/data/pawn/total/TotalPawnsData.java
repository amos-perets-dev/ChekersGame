package com.example.chekersgamepro.data.pawn.total;

public class TotalPawnsData {

    private int regularPawns;
    private int queenPawns;

    public TotalPawnsData(int regularPawns, int queenPawns) {
        this.regularPawns = regularPawns;
        this.queenPawns = queenPawns;
    }

    public int getRegularPawns() {
        return regularPawns;
    }

    public int getQueenPawns() {
        return queenPawns;
    }

}
