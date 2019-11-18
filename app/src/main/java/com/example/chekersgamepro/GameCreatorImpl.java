package com.example.chekersgamepro;

import android.graphics.Point;
import android.util.Pair;

import com.example.chekersgamepro.data.cell.CellDataImpl;
import com.example.chekersgamepro.data.game_validation.GameValidationImpl;
import com.example.chekersgamepro.data.pawn.PawnDataImpl;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GameCreatorImpl implements GameManager.ChangePlayerListener {

    private List<Point> cellsRelevantStart = new ArrayList<>();

    private Map<Point, Pair<List<Point>, List<PawnDataImpl>>> listsAllOptionalPathByCell = new HashMap<>();

    /**
     * The optional cells that the pawn can move on it
     */
    private List<Point> listOptionalCellsPathTmp = new ArrayList<>();//

    private List<PawnDataImpl> removeListPawnTmp = new ArrayList<>();

    private List<DataCellViewClick> dataOptionalPathByView = new ArrayList<>();//

    private List<PawnDataImpl> removeListPawn = new ArrayList<>();//

    private DataGame dataGame = DataGame.getInstance();

    private GameValidationImpl gameValidation;

    private CellDataImpl prevCellData;

    private boolean isLastOptionalPathValid = false;

    private boolean isPlayerOneTurn;

    public void clearData(){
        cellsRelevantStart.clear();

        listsAllOptionalPathByCell.clear();

        listOptionalCellsPathTmp.clear();

        dataOptionalPathByView.clear();

        removeListPawn.clear();

    }

    public GameCreatorImpl(boolean isPlayerOneTurn
            , GameValidationImpl gameValidation
            , List<GameManager.ChangePlayerListener> changePlayerListListeners) {
        this.isPlayerOneTurn = isPlayerOneTurn;
        this.gameValidation = gameValidation;
        changePlayerListListeners.add(this);
    }


    public List<Point> createRelevantCellsStart() {
        cellsRelevantStart.clear();

        // TODO check the start cell
        FluentIterable.from((isPlayerOneTurn
                    ? dataGame.getPawnsPlayerOne().values()
                    : dataGame.getPawnsPlayerTwo().values()))
                .transform(PawnDataImpl::getContainerCellXY)
                .transform(dataGame::getCellByPoint)
                .filter(gameValidation::isCanCellStart)
                .filter(cell -> !cell.isEmpty())
                .transform(CellDataImpl::getPoint)
                .transform(cellsRelevantStart::add)
                .toList();

        return cellsRelevantStart;
    }

    public List<DataCellViewClick> createOptionalPath(float x, float y){

        dataOptionalPathByView.clear();
        listsAllOptionalPathByCell.clear();

        CellDataImpl currCellData =  dataGame.getCellByPoint(new Point((int) x, (int) y));

        // check if the pawn is null
        // because if the user click when another process is running the pawn is null
        if (currCellData == null) return null;

        // check if point contains in the can be start cells
        if (!cellsRelevantStart.contains(currCellData.getPoint())) {
            addDataOptionalPath(false, currCellData);
            isLastOptionalPathValid = false;
            return dataOptionalPathByView;
        }

        this.prevCellData = currCellData;

        // add the first/root cell
        addDataOptionalPath(true, currCellData);

        // add the first/root cell
        listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());
        createOptionalPathByCell(getNextCell(currCellData, true), true);

        // add the first/root cell
        listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());
        createOptionalPathByCell(getNextCell(currCellData, false), true);

        isLastOptionalPathValid = true;

        return dataOptionalPathByView;

    }

    private void createOptionalPathByCell(CellDataImpl currCellData, boolean isFromRoot){

        if (currCellData == null)return;

        // check the curr cell after the root if is empty is valid but the end.
        // if the curr cell is leaf is valid but the end
        if ((currCellData.isEmpty() && isFromRoot) || gameValidation.isLeaf(currCellData)){
            addDataOptionalPath(true, currCellData);
            listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());

            List<Point> pointListMovePawnTmp = new ArrayList<>(listOptionalCellsPathTmp);
            List<PawnDataImpl> removeListDataPawnTmp = new ArrayList<>(removeListPawnTmp);
            listsAllOptionalPathByCell.put(currCellData.getPoint(), Pair.create(pointListMovePawnTmp, removeListDataPawnTmp));
            listOptionalCellsPathTmp.clear();
            removeListPawnTmp.clear();
            return;
        } else {

            //check the cell after the root
            if (!currCellData.isEmpty() && isFromRoot && !gameValidation.isEqualPlayerCells(currCellData)){
                CellDataImpl nextCellDataByCell;

                //calculate the diagonal direction
                boolean isRightDiagonal = currCellData.getPoint().x > prevCellData.getPoint().x;
                if (isRightDiagonal){
                    nextCellDataByCell = getNextCell(currCellData, false);
                } else {
                    nextCellDataByCell = getNextCell(currCellData, true);
                }
                if (nextCellDataByCell != null && nextCellDataByCell.isEmpty()){

                    addDataOptionalPath(true, currCellData);

                    removeListPawnTmp.add(dataGame.getPawnByPoint(currCellData.getPointStartPawn()));

//                    listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());
                    prevCellData = currCellData;
                    createOptionalPathByCell(nextCellDataByCell, false);
                }
            }

            // check the cell is empty but not leaf(maybe it a node)
            // need to check the right and left next cell
            if (currCellData.isEmpty()){
                addDataOptionalPath(true, currCellData);
                listOptionalCellsPathTmp.add(currCellData.getPointStartPawn());

                prevCellData = currCellData;
                CellDataImpl nextCellDataByCellRight = getNextCell(currCellData, false);
                if (nextCellDataByCellRight != null && !nextCellDataByCellRight.isEmpty() && !gameValidation.isEqualPlayerCells(nextCellDataByCellRight)){
                    createOptionalPathByCell(nextCellDataByCellRight, true);
                }

                CellDataImpl nextCellDataByCellLeft = getNextCell(currCellData, true);
                if (nextCellDataByCellLeft != null && !nextCellDataByCellLeft.isEmpty() && !gameValidation.isEqualPlayerCells(nextCellDataByCellLeft)) {
                    createOptionalPathByCell(nextCellDataByCellLeft, true);
                }
            }

        }

    }



    private CellDataImpl getNextCell(CellDataImpl cellData, boolean isLeft){
            return cellData != null
                    ? isPlayerOneTurn
                        ? isLeft
                            ? cellData.getNextCellDataLeftPlayerOne()
                            : cellData.getNextCellDataRightPlayerOne()
                        : isLeft
                            ? cellData.getNextCellDataLeftPlayerTwo()
                            : cellData.getNextCellDataRightPlayerTwo()
                    : null;

    }

    private @ Nullable Object addDataOptionalPath(boolean isClickValid, @Nullable CellDataImpl cellData) {
        if (cellData == null)return null;
        dataOptionalPathByView.add(new DataCellViewClick(isClickValid,cellData.getPoint(), dataOptionalPathByView.size() == 0, cellData.isEmpty()));
        return new Object();
    }

    @Override
    public void onChangePlayer(boolean isPlayerOneTurn) {
        this.isPlayerOneTurn = isPlayerOneTurn;
    }

    /**
     * Checked if the point in the optional path or not
     *
     * @param x from current point cell
     * @param y from current point cell
     * @return
     */
    public boolean isInOptionalPath(float x, float y) {

        if (!isLastOptionalPathValid){
            dataOptionalPathByView.clear();
            return false;
        }

        CellDataImpl cellByPoint = dataGame.getCellByPoint(new Point((int) x, (int) y));
        // check if the pawn is null
        // because if the user click when another process is running the pawn is null
        if (cellByPoint == null) return false;
        Point currPoint = cellByPoint.getPointStartPawn();

        return FluentIterable.from(listsAllOptionalPathByCell.values())
                .transform(new Function<Pair<List<Point>, List<PawnDataImpl>>, List<Point>>() {
                    @Nullable
                    @Override
                    public List<Point> apply(@Nullable Pair<List<Point>, List<PawnDataImpl>> input) {
                        return input.first;
                    }
                })
                .filter(list -> list.contains(currPoint))
                .first()
                .isPresent();

    }

    /**
     * Check if the point is the end point in the path
     * @param x from current point cell
     * @param y from current point cell
     * @return
     */
    public boolean isInOptionalPathValidCell(float x, float y) {

      return listsAllOptionalPathByCell.get(new Point((int) x, (int) y)) != null;
    }

    CellDataImpl cellDataSrc;
    CellDataImpl cellDataDst;
    PawnDataImpl pawnDataStart;
    /**
     *
     *
     * @param x point of the dst cell
     * @param y point of the dst cell
     * @return
     */
    public Pair<Point,  List<Point>> getMovePawnPath(float x, float y) {
        indexRemovePawnList = 0;
        removeListPawn.clear();

        Point currPointFromUser = new Point((int) x, (int) y);
        Pair<List<Point>, List<PawnDataImpl>> listListPair = listsAllOptionalPathByCell.get(currPointFromUser);
        List<Point> pointListMovePawnPath = listListPair.first;
        List<PawnDataImpl> pawnDataListToRemove = listsAllOptionalPathByCell.get(currPointFromUser).second;

        cellDataSrc =  dataGame.getCellByPoint(dataGame.getPawnByPoint(pointListMovePawnPath.get(0)).getContainerCellXY());
        cellDataDst =  dataGame.getCellByPoint(currPointFromUser);

        pawnDataStart = dataGame.getPawnByPoint(cellDataSrc.getPointStartPawn());

        // check if the pawn is null
        // because if the user click when another process is running the pawn is null
        if (pawnDataStart == null) return null;

        Point pointStartPawn = pawnDataStart.getStartXY();

        //create list, for the move the pawn on the empties cells
        FluentIterable.from(pawnDataListToRemove)
                .transform(removeListPawn::add)
                .toList();

//        //SET DATA <<<
//        dataGame.updateCell(cellDataDst, false, isPlayerOneTurn );
//        dataGame.updateCell(cellDataSrc, true, false);
//        dataGame.updatePawn(pawnDataStart, cellDataDst);
//        //SET DATA >>>


        return new Pair<>(pointStartPawn, pointListMovePawnPath);
    }

    public void setCurrentTurnData(){
        //SET DATA <<<
        dataGame.updateCell(cellDataDst, false, isPlayerOneTurn );
        dataGame.updateCell(cellDataSrc, true, false);
        dataGame.updatePawn(pawnDataStart, cellDataDst);
        //SET DATA >>>
    }

    private int indexRemovePawnList = 0;

    /**
     * Remove the relevant pawn from the data
     * the point param is the current point(pawn),
     * and check if the next cell in the list is container the pawn tat need to be removed
     *
     * @return
     */
    public PawnDataImpl removePawnIfNeeded() {

        PawnDataImpl pawnData = null;
        if (indexRemovePawnList < removeListPawn.size()){
            pawnData = removeListPawn.get(indexRemovePawnList);
            indexRemovePawnList++;
            dataGame.updatePawnKilled(pawnData);
        }
        return pawnData;

    }
}
