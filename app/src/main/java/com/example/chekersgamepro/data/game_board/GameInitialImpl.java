package com.example.chekersgamepro.data.game_board;

import android.graphics.Color;
import android.graphics.Point;

import com.example.chekersgamepro.DataGame;
import com.example.chekersgamepro.data.BorderLine;
import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameInitialImpl {

    private final int GAME_BOARD_SIZE = 8;
    private final int DIV_SIZE_CELL = 14;
    private final int COLOR_BORDER_CELL = Color.BLACK;
    private final int BORDER_WIDTH = 2;

    private DataGame dataGame = DataGame.getInstance();

    private int width;
    private int height;
    private int x = 0;
    private int y = 0;

    private List<BorderLine> borderLines = new ArrayList<>();

    private int widthCell;
    private int heightCell;

    public GameInitialImpl() {
    }

    public Map<Point, PawnDataImpl> getPawns() {
        return dataGame.getPawns();
    }

    public int getWidth() {
        return width;
    }

    public GameInitialImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public GameInitialImpl setHeight(int height) {
        this.height = height;
        return this;
    }

    public GameInitialImpl setX(int x) {
        this.x = x;
        return this;
    }

    public GameInitialImpl setY(int y) {
        this.y = y;
        return this;
    }

    public int getGameBoardSize() {
        return GAME_BOARD_SIZE;
    }

    public int getColorBorderCell() {
        return COLOR_BORDER_CELL;
    }

    public int getBorderWidth() {
        return BORDER_WIDTH;
    }


    public int getWidthCell() {
        return widthCell;
    }

    public int getHeightCell() {
        return heightCell;
    }

    public List<BorderLine> getBorderLines() {
        return borderLines;
    }

    public Map<Point, CellDataImpl> getCells() {
        return dataGame.getCells();
    }

    /**
     * Init the pawns on the board game
     **/
    public void initPawns() {

        int i = 0;
        boolean isNeedPutPawn;

        int factorDecreaseSizeCell = (heightCell - BORDER_WIDTH) / DIV_SIZE_CELL;

        Map<Point, CellDataImpl> cells = getCells();
        for (Map.Entry<Point, CellDataImpl> cellDataEntry : cells.entrySet()){
            CellDataImpl cellData = cellDataEntry.getValue();

            isNeedPutPawn = cellData.isValidCell() && !cellData.isEmpty() && !cellData.isEmptyFirstTimeDraw();
            if (isNeedPutPawn){

                PawnDataImpl pawnData = new PawnDataImpl(i
                        , cellData.getPoint()
                        , cellData.isPlayerOneCurrently()
                        , cellData.getPointStartPawn()
                        , widthCell - factorDecreaseSizeCell - BORDER_WIDTH
                        , heightCell - factorDecreaseSizeCell - BORDER_WIDTH);

                dataGame.putPawnByPlayer(pawnData);
            }
            i++;

        }
    }

    public void initBorderLines(){

        // measure the all inside borders between the cells
        int insideBorders = (GAME_BOARD_SIZE + 1) * BORDER_WIDTH;

        // measure size cell without the border
        widthCell = ((width - insideBorders) / GAME_BOARD_SIZE) ;
        heightCell = ((height - insideBorders) / GAME_BOARD_SIZE) ;

        // initGameBoard the first x,y point
        int tmpX = 0 ;
        int tmpY = BORDER_WIDTH / 2 ;

        // init rows
        for (int i = 0; i < GAME_BOARD_SIZE + 1; i++){
            Point startPoint = new Point(0, tmpY);
            Point endPoint = new Point(tmpX + ((widthCell + BORDER_WIDTH) * GAME_BOARD_SIZE) + BORDER_WIDTH , tmpY);
            BorderLine borderLine = new BorderLine(startPoint, endPoint);
            borderLines.add(borderLine);
            tmpY +=  heightCell + BORDER_WIDTH;
        }

        // initGameBoard the first x,y point
        tmpX = BORDER_WIDTH / 2 ;
        tmpY = 0;

        // init columns
        for (int i = 0; i < GAME_BOARD_SIZE + 1; i++){
            Point startPoint = new Point(tmpX, 0);
            Point endPoint = new Point(tmpX  , tmpY + ((heightCell + BORDER_WIDTH) * GAME_BOARD_SIZE) + BORDER_WIDTH);
            BorderLine borderLine = new BorderLine(startPoint, endPoint);
            borderLines.add(borderLine);
            tmpX += widthCell + BORDER_WIDTH;
        }

    }

    public void initGameBoard() {

        boolean isDarkCell;
        boolean isValidSquare;
        boolean isEmptyFirstTimeDraw;
        boolean isMasterSquare;
        boolean isEmpty;
        boolean isReplaceFirstColor;
        boolean isPlayerOnCurrently = false;

       int  insideBorders = 0;
        // measure size cell without the border
        widthCell = ((width - insideBorders) / GAME_BOARD_SIZE) ;
        heightCell = ((height - insideBorders) / GAME_BOARD_SIZE) ;

        // initGameBoard the first x,y point
          int tmpX = x ;
         int tmpY = y ;

        /**
         * Get the start x,y pawn
         */
        int factorDecreaseSizeCell = (heightCell - BORDER_WIDTH) / DIV_SIZE_CELL;

         for (int i = 0; i < GAME_BOARD_SIZE; i++) {

            //check if need to replace the first color in the row
            isReplaceFirstColor = i % 2 == 0;

            for (int j = 0; j < GAME_BOARD_SIZE; j++) {

            //change color
                if (j % 2 == 0) {
                    isDarkCell = !isReplaceFirstColor;
                    isValidSquare = !isReplaceFirstColor;

                } else {
                    isDarkCell = isReplaceFirstColor;
                    isValidSquare = isReplaceFirstColor;
                }

                Point point = new Point(tmpX + (BORDER_WIDTH )  , tmpY  + BORDER_WIDTH);

                isEmptyFirstTimeDraw = ((i == GAME_BOARD_SIZE / 2) || (i == GAME_BOARD_SIZE / 2 - 1));

                isMasterSquare = ((i == GAME_BOARD_SIZE - 1) || (i == 0));
                isEmpty = !(isValidSquare && !isEmptyFirstTimeDraw);

                if (isValidSquare && !isEmpty){
                    isPlayerOnCurrently = i < GAME_BOARD_SIZE / 2 - 1;
                }

                Point pointStartPawn = new Point(point.x + factorDecreaseSizeCell / 2, (point.y + factorDecreaseSizeCell / 2));

                CellDataImpl cellData = new CellDataImpl( isValidSquare
                        , isEmpty
                        , isDarkCell
                        , point
                        , isEmptyFirstTimeDraw
                        , isMasterSquare
                , widthCell - BORDER_WIDTH
                , heightCell - BORDER_WIDTH
                , isPlayerOnCurrently
                , pointStartPawn);

                dataGame.putCellByPlayer(cellData);

                isPlayerOnCurrently = false;

                // initGameBoard the nex column
                tmpX += widthCell;
            }

            // initGameBoard the next row
            tmpY = (y ) + (heightCell  ) * (i + 1);
            tmpX = x ;
        }

        Map<Point, CellDataImpl> cells = getCells();
        for (Map.Entry<Point, CellDataImpl> cellDataEntry : cells.entrySet()){
            CellDataImpl cellData = cellDataEntry.getValue();
            setNextCellByCell(cellData);
        }

    }


    public void setNextCellByCell(CellDataImpl cellData) {

        int pointX;
        int pointY;

        if (cellData.isValidCell()) {

            Point pointCurrCell = cellData.getPoint();

            //set By PLayer one
            pointX = pointCurrCell.x - widthCell;
            pointY = pointCurrCell.y + heightCell;

            CellDataImpl nextCellLeftPlayerOne = dataGame.getCellByPoint(new Point(pointX, pointY));

            pointX = pointCurrCell.x + widthCell;
            pointY = pointCurrCell.y + heightCell;
            CellDataImpl nextCellRightPlayerOne = dataGame.getCellByPoint(new Point(pointX, pointY));

            //set By Player two
            pointX = pointCurrCell.x - widthCell;
            pointY = pointCurrCell.y - heightCell;
            CellDataImpl nextCellLeftPlayerTwo = dataGame.getCellByPoint(new Point(pointX, pointY));

            pointX = pointCurrCell.x + widthCell;
            pointY = pointCurrCell.y - heightCell;
            CellDataImpl nextCellRightPlayerTwo = dataGame.getCellByPoint(new Point(pointX, pointY));

            cellData.setNextCellDataLeftPlayerOne(nextCellLeftPlayerOne)
                    .setNextCellDataRightPlayerOne(nextCellRightPlayerOne)
                    .setNextCellDataLeftPlayerTwo(nextCellLeftPlayerTwo)
                    .setNextCellDataRightPlayerTwo(nextCellRightPlayerTwo);
        }

        dataGame.putCellByPlayer(cellData);
    }
}
