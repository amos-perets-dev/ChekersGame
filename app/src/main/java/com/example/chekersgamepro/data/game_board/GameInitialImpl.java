package com.example.chekersgamepro.data.game_board;

import android.graphics.Color;
import android.graphics.Point;

import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;

public class GameInitialImpl {
    private final int GAME_BOARD_SIZE = DataGame.GAME_BOARD_SIZE;
    private final int DIV_SIZE_CELL = 14;
    private final int COLOR_BORDER_CELL = Color.BLACK;
    private final int BORDER_WIDTH = 2;

    private DataGame dataGame;

    private int width;
    private int height;
    private int x;
    private int y;

    private int widthCell;
    private int heightCell;

    private CellDataImpl[][] boardCells;

    private Map<Point, CellDataImpl> cells;

    public GameInitialImpl(int x, int y, int width, int height, int gameMode) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        boardCells = new CellDataImpl[GAME_BOARD_SIZE][GAME_BOARD_SIZE];
        dataGame = DataGame.getInstance();
        dataGame.setGameMode(gameMode);

        cells = new HashMap<>();
    }

    public Completable init(){
        return initBorderLines()
                .andThen(initGameBoardCells())
                .andThen(initPawns());
    }

    /**
     * Init the pawns on the board game
     *
     * @return*/
    private Completable initPawns() {
        dataGame.clearCachePawns();

        int i = 0;

        int factorDecreaseSizeCell = (heightCell - BORDER_WIDTH) / DIV_SIZE_CELL;

        Map<Point, CellDataImpl> cells = dataGame.getCells();
        for (Map.Entry<Point, CellDataImpl> cellDataEntry : cells.entrySet()) {
            CellDataImpl cellData = cellDataEntry.getValue();

            if (cellData.getCellContain() == DataGame.CellState.PLAYER_ONE
                    || cellData.getCellContain() == DataGame.CellState.PLAYER_TWO) {

                PawnDataImpl pawnData = new PawnDataImpl(i
                        , cellData.getPointCell()
                        , cellData.getCellContain()
                        , cellData.getPointStartPawn()
                        , widthCell - factorDecreaseSizeCell - BORDER_WIDTH
                        , heightCell - factorDecreaseSizeCell - BORDER_WIDTH);

                dataGame.putPawnByPlayer(pawnData);
            }
            i++;

        }

        return Completable.complete();
    }

    private Completable initBorderLines() {
        List<BorderLine> borderLines = new ArrayList<>();

        // measure the all inside borders between the cells
        int insideBorders = (GAME_BOARD_SIZE + 1) * BORDER_WIDTH;

        // measure size cell without the border
        widthCell = ((width - insideBorders) / GAME_BOARD_SIZE);
        heightCell = ((height - insideBorders) / GAME_BOARD_SIZE);

        // initGameBoard the first x,y point
        int tmpX = 0;
        int tmpY = BORDER_WIDTH / 2;

        // init rows
        for (int i = 0; i < GAME_BOARD_SIZE + 1; i++) {
            Point startPoint = new Point(0, tmpY);
            Point endPoint = new Point(tmpX + ((widthCell + BORDER_WIDTH) * GAME_BOARD_SIZE) + BORDER_WIDTH, tmpY);
            BorderLine borderLine = new BorderLine(startPoint, endPoint);
            borderLines.add(borderLine);
            tmpY += heightCell + BORDER_WIDTH;
        }

        // initGameBoard the first x,y point
        tmpX = BORDER_WIDTH / 2;
        tmpY = 0;

        // init columns
        for (int i = 0; i < GAME_BOARD_SIZE + 1; i++) {
            Point startPoint = new Point(tmpX, 0);
            Point endPoint = new Point(tmpX, tmpY + ((heightCell + BORDER_WIDTH) * GAME_BOARD_SIZE) + BORDER_WIDTH);
            BorderLine borderLine = new BorderLine(startPoint, endPoint);
            borderLines.add(borderLine);
            tmpX += widthCell + BORDER_WIDTH;
        }

        dataGame.setBorderLines(borderLines);

        return Completable.complete();

    }

    private Completable initGameBoardCells() {
        dataGame.clearCacheCells();
        cells.clear();

        boolean isMasterCell = false;
        int cellContain;

        int insideBorders = 0;
        // measure size cell without the border
        widthCell = ((width - insideBorders) / GAME_BOARD_SIZE);
        heightCell = ((height - insideBorders) / GAME_BOARD_SIZE);

        // initGameBoard the first x,y point
        int tmpX = x;
        int tmpY = y;


        // start x,y pawn
        int factorDecreaseSizeCell = (heightCell - BORDER_WIDTH) / DIV_SIZE_CELL;

        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            // set the master cell
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
                Point pointCell = new Point(tmpX + (BORDER_WIDTH), tmpY + BORDER_WIDTH);
                Point pointStartPawn = new Point(pointCell.x + factorDecreaseSizeCell / 2, (pointCell.y + factorDecreaseSizeCell / 2));

                if (row % 2 != column % 2) {
                    isMasterCell = row == GAME_BOARD_SIZE - 1 || row == 0;
                    if (row < 3) {
                        cellContain = DataGame.CellState.PLAYER_ONE;
                    } else if (row > 4) {
                        cellContain = DataGame.CellState.PLAYER_TWO;
                    } else {
                        cellContain = DataGame.CellState.EMPTY;
                    }
                } else {
                    cellContain = DataGame.CellState.EMPTY_INVALID;
                }

//                Log.d("TEST_GAME", "CELL CONTAIN: " + cellContain + ", idCell: " + idCell);
                int idCell = (row * GAME_BOARD_SIZE) + column + 1;

                CellDataImpl cellData = new CellDataImpl(idCell
                        , cellContain
                        , pointCell
                        , pointStartPawn
                        , isMasterCell
                        , widthCell - BORDER_WIDTH
                        , heightCell - BORDER_WIDTH);

                this.boardCells[row][column] = cellData;
                cells.put(cellData.getPointCell(), cellData);
                tmpX += widthCell;
                isMasterCell = false;
            }
            // initGameBoard the next row
            tmpY = (y) + (heightCell) * (row + 1);
            tmpX = x;
            isMasterCell = false;
        }

        // set he next cell by cell
        for (int row = 0; row < GAME_BOARD_SIZE; row++) {
            for (int column = 0; column < GAME_BOARD_SIZE; column++) {
                CellDataImpl cellData = setNextCellByCell(this.boardCells[row][column]);
                this.boardCells[row][column] = cellData;
                dataGame.putCellByPlayer(cellData);
                dataGame.puCellById(cellData.getIdCell(), cellData.getPointCell());
            }
        }

        dataGame.setBoardCells(this.boardCells);

        return Completable.complete();

    }

    private CellDataImpl setNextCellByCell(CellDataImpl cellData) {

        int pointX;
        int pointY;

        if (cellData.getCellContain() != DataGame.CellState.EMPTY_INVALID) {

            Point pointCurrCell = cellData.getPointCell();

            //set By PLayer one
            pointX = pointCurrCell.x - widthCell;
            pointY = pointCurrCell.y + heightCell;

            CellDataImpl nextCellLeftPlayerOne = cells.get(new Point(pointX, pointY));

            pointX = pointCurrCell.x + widthCell;
            pointY = pointCurrCell.y + heightCell;
            CellDataImpl nextCellRightPlayerOne = cells.get(new Point(pointX, pointY));

            //set By Player two
            pointX = pointCurrCell.x - widthCell;
            pointY = pointCurrCell.y - heightCell;
            CellDataImpl nextCellLeftPlayerTwo = cells.get(new Point(pointX, pointY));

            pointX = pointCurrCell.x + widthCell;
            pointY = pointCurrCell.y - heightCell;
            CellDataImpl nextCellRightPlayerTwo = cells.get(new Point(pointX, pointY));

            cellData.setNextCellDataLeftBottom(nextCellLeftPlayerOne)
                    .setNextCellDataRightBottom(nextCellRightPlayerOne)
                    .setNextCellDataLeftTop(nextCellLeftPlayerTwo)
                    .setNextCellDataRightTop(nextCellRightPlayerTwo);
        }

        return cellData;

    }

}