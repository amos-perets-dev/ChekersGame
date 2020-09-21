package com.example.chekersgamepro.data.pawn.total;

public class TotalPawnsDataByPlayer {

    private TotalPawnsData totalPawnsDataPlayerOne;

    private TotalPawnsData totalPawnsDataPlayerTwo;

    public TotalPawnsDataByPlayer(TotalPawnsData totalPawnsDataPlayerOne, TotalPawnsData totalPawnsDataPlayerTwo) {
        this.totalPawnsDataPlayerOne = totalPawnsDataPlayerOne;
        this.totalPawnsDataPlayerTwo = totalPawnsDataPlayerTwo;
    }

    public String getTotalPawnsPlayerOne() {
        return String.valueOf(totalPawnsDataPlayerOne.getRegularPawns() + totalPawnsDataPlayerOne.getQueenPawns());
    }

    public String getTotalPawnsPlayerTwo() {
        return String.valueOf(totalPawnsDataPlayerTwo.getRegularPawns() + totalPawnsDataPlayerTwo.getQueenPawns());
    }

    public String getRegularPawnsPlayerOne() {
        return String.valueOf(totalPawnsDataPlayerOne.getRegularPawns());
    }

    public String getQueenPawnsPlayerOne() {
        return String.valueOf(totalPawnsDataPlayerOne.getQueenPawns());
    }

    public String getRegularPawnsPlayerTwo() {
        return String.valueOf(totalPawnsDataPlayerTwo.getRegularPawns());
    }

    public String getQueenPawnsPlayerTwo() {
        return String.valueOf(totalPawnsDataPlayerTwo.getQueenPawns());
    }

}
