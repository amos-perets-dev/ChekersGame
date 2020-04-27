package com.example.chekersgamepro.screens.game.views

import android.app.Activity
import android.graphics.Point
import android.util.Log
import android.util.Pair
import android.view.View
import com.example.chekersgamepro.data.cell.CellDataImpl
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.data.game_board.GameInitialImpl
import com.example.chekersgamepro.data.pawn.pawn.PawnDataImpl
import com.example.chekersgamepro.graphic.cell.CellView
import com.example.chekersgamepro.graphic.game_board.GameBoardView
import com.example.chekersgamepro.graphic.pawn.PawnView
import com.example.chekersgamepro.screens.game.CheckersViewModel
import com.google.common.collect.FluentIterable
import io.reactivex.Completable
import io.reactivex.Observable
import org.checkerframework.checker.nullness.qual.Nullable
import java.util.*


class GameBoardViews(private val gameBoardView: GameBoardView
                     , private val checkersViewModel: CheckersViewModel
                     , private val activity: Activity) {

    private var indexCellView = 0

    private var indexPawnView = 0

    private var cellViewMap: HashMap<Point, CellView> = HashMap()

    private var pawnViewMap: HashMap<Point, PawnView> = HashMap()

    fun initCellsAndPawns(): Completable {
        return initCellsViews()
                .doOnEvent { Log.d("TEST_GAME", "initCellsViews()") }
                .andThen(initPawnsViews()
                        .doOnEvent { Log.d("TEST_GAME", "initPawnsViews()") })
                .andThen(gameBoardView.drawBorders(
                        checkersViewModel.borderLines
                        , checkersViewModel.borderWidth
                        , checkersViewModel.colorBorderCell)
                        .doOnEvent { Log.d("TEST_GAME", "gameBoardView.drawBorders()") })
    }

    private fun initCellsViews(): Completable {
        cellViewMap = HashMap()

        val entries = checkersViewModel.cells.entries

//        Log.e("TEST_GAME", "entries size: ${entries.size} STACK: ${Log.getStackTraceString(Throwable())}")

        FluentIterable.from(entries)
                .transform { it!!.value }
                .transform { createPairCellDataAndView(it!!) }
                .transform { initCellView(it!!) }
                .transform {
                    cellViewMap.put(it!!.first, it.second)
                    ""
                }
                .toList()
        return Completable.complete()
    }

    private fun createPairCellDataAndView(cellData: CellDataImpl): Pair<CellDataImpl, CellView> {
        val id: Int = activity.resources.getIdentifier("cell" + (indexCellView + 1), "id", activity.packageName)
        val cellView = activity.findViewById<CellView>(id)

//        Log.d("TEST_GAME", "cellView: $cellView, id: $id, indexCellView: $indexCellView")

        indexCellView++

        return Pair(cellData, cellView)
    }

    private fun initCellView(input: @Nullable Pair<CellDataImpl, CellView>): Pair<Point, CellView> {
        val cellData = input.first
        var second = input.second

        val cellView = second
                .setWidth(cellData.widthCell)
                .setHeight(cellData.heightCell)
                .setBg(cellData.alphaCell, cellData.isMasterCell)
                .setXY(cellData.pointCell.x, cellData.pointCell.y)
                .setIsCanClick(cellData.cellContain != DataGame.CellState.EMPTY_INVALID)
        return Pair(cellData.pointCell, cellView)
    }

    private fun initPawnsViews(): Completable {
        pawnViewMap = HashMap()

        FluentIterable.from(checkersViewModel.pawns.entries)
                .transform { it!!.value }
                .transform { createPairPawnDataAndView(it!!) }
                .transform { initPawnView(it!!) }
                .transform {
                    pawnViewMap.put(it!!.first, it.second)
                    ""
                }
                .toList()

        return Completable.complete()
    }

    private fun createPairPawnDataAndView(pawnData: PawnDataImpl): Pair<PawnDataImpl, PawnView> {
        val id: Int = activity.getResources().getIdentifier("pawn" + (indexPawnView + 1), "id", activity.getPackageName())
        indexPawnView++
        return Pair(pawnData, activity.findViewById(id))
    }

    private fun initPawnView(input: Pair<PawnDataImpl, PawnView>): Pair<Point, PawnView> {
        val pawnData = input.first
        val pawnView = input.second
        //        pawnView.setVisibility(View.GONE);
        pawnView
                .setWidth(pawnData!!.width)
                .setHeight(pawnData.height)
                .setRegularIcon(pawnData.regularIcon)
                .setQueenIcon(pawnData.queenIcon)
                .setXY(pawnData.startXY.x, pawnData.startXY.y)
                .setIsReady(true)
        return Pair(pawnData.startXY, pawnView)
    }


    fun addViewsToObservable(): MutableList<Observable<out View>> {

        val viewsList: ArrayList<Observable<out View>> = java.util.ArrayList()

        val toListCell = FluentIterable.from(cellViewMap.values)
                .transform { it!!.cellClick }
                .toList()
        val toListPawn = FluentIterable.from(pawnViewMap.values)
                .transform { it!!.pawnClick }
                .toList()
        viewsList.addAll(toListCell)
        viewsList.addAll(toListPawn)
        return viewsList
    }

    /**
     * Update the pawn view by point when the animate move finished
     *
     * @param point the path end point
     */
    fun updatePawnViewStart(point: Point, currPointPawnViewStartPath: Point?, currPawnViewStartPath: PawnView) {
        pawnViewMap.remove(currPointPawnViewStartPath)
        currPawnViewStartPath.setXY(point.x, point.y)
        // now the curr point is the end point
        pawnViewMap[point] = currPawnViewStartPath
        currPawnViewStartPath.setIcon(checkersViewModel.isQueenPawn(point))
    }

    fun getPawn(pointPawnByCell: Point?): PawnView? {
        return pawnViewMap[pointPawnByCell]
    }

    fun removePawnView(point: Point?) {
        pawnViewMap[point]!!.removePawn()
    }

    fun getCellViewByPoint(point: Point?): CellView? {
        return if (cellViewMap == null) null else cellViewMap[point]
    }

    fun clearCheckedCell(point: Point?) {
        checkedCell(point, DataGame.ColorCell.CLEAR_CHECKED)
    }

    fun checkedCell(point: Point?, color: Int) {
        val cellView = cellViewMap[point]
        cellView?.checked(color)
    }
}