package com.example.chekersgamepro.screens.game.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Pair
import android.view.View
import android.widget.TextView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersApplication.Factory.create
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.data.DataCellViewClick
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.data.game_board.GameInitialImpl
import com.example.chekersgamepro.graphic.cell.CellView
import com.example.chekersgamepro.graphic.game_board.GameBoardView
import com.example.chekersgamepro.graphic.pawn.PawnView
import com.example.chekersgamepro.models.player.game.PlayerGame
import com.example.chekersgamepro.screens.game.CheckersViewModel
import com.example.chekersgamepro.screens.game.StartGameDialog
import com.example.chekersgamepro.screens.game.model.GameFinishData
import com.google.common.collect.FluentIterable
import com.google.common.collect.ImmutableList
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.internal.functions.Functions

class CheckersGameViewsManager(
        private val activity: Activity
        , private val checkersViewModel: CheckersViewModel
        , compositeDisposable: CompositeDisposable
        , intent: Intent) {
    private var textViewTestStart: TextView? = null
    private lateinit var computerIconView: ComputerIconView
    private var gameBoardContainer: View? = null
    private lateinit var playersNamesView: PlayersNamesView
    private var viewsByRelevantCellsList: ImmutableList<CellView>? = null
    private lateinit var timerView: ProgressTimeView
    private lateinit var gameBoardViews: GameBoardViews
    private lateinit var startGameDialog: StartGameDialog
    private val gameMode: Int
    private lateinit var gameBoardView: GameBoardView

    fun initGame(): Observable<Any> {
        return gameBoardView.onDrawFinish()
                .distinctUntilChanged()
                .doOnNext { startGameDialog.showDialogGame() }
                .flatMap { gameBoardView: GameBoardView? ->
                    startGameDialog.isShowDialogGame()
                            .flatMap(Function<Boolean, ObservableSource<*>> { isShowDialogGame ->
                                val completable = if (isShowDialogGame) initViews() else Completable.complete()
                                return@Function completable
                                        .andThen(if (isShowDialogGame) Completable.complete() else showViews())
                                        .andThen(Observable.just(isShowDialogGame))
                                        .filter(Functions.equalsWith(false))
                            }
                            )
                }
    }

    private fun initViews(): Completable {
        return initGameBoardCellsPawns(gameMode)
                .andThen(gameBoardViews.initCellsAndPawns())
                .andThen(computerIconView.initComputerIcon(gameMode, timerView.getHeight()))
    }

    private fun findViews(activity: Activity) {
        gameBoardView = activity.findViewById(R.id.game_board_view)
        gameBoardContainer = activity.findViewById(R.id.container_game_board)
        textViewTestStart = activity.findViewById(R.id.text_test_start)
    }

    fun setPlayerTurn(isPlayerOneTurn: Boolean) {
        playersNamesView.setPlayerTurn(isPlayerOneTurn)
    }

    fun addViewsToObservable(ignored: Any?): List<Observable<out View>> {
        return gameBoardViews.addViewsToObservable()
    }

    /**
     * Update the pawn view by point when the animate move finished
     *
     * @param point the path end point
     */
    private fun updatePawnViewStart(point: Point, currPointPawnViewStartPath: Point, currPawnViewStartPath: PawnView) {
        gameBoardViews.updatePawnViewStart(point, currPointPawnViewStartPath, currPawnViewStartPath)
    }

    fun getPawn(pointPawnByCell: Point?): PawnView? {
        return gameBoardViews.getPawn(pointPawnByCell)
    }

    fun removePawnView(point: Point?) {
        gameBoardViews.removePawnView(point)
    }

    fun checkedCell(point: Point?, color: Int, drawQueen: Boolean) {
        gameBoardViews.checkedCell(point, color, drawQueen)
    }

    private fun initGameBoardCellsPawns(gameMode: Int): Completable {
        return GameInitialImpl(
                gameBoardView.x.toInt()
                , gameBoardView.y.toInt()
                , gameBoardView.measuredWidth
                , gameBoardView.measuredHeight
                , gameMode)
                .init()
                .andThen(checkersViewModel.initGame())
    }

    private fun clearCheckedCellsPath(cellsViewOptionalPath: List<DataCellViewClick>) {
        for (cellViewClick in cellsViewOptionalPath) {
            gameBoardViews.clearCheckedCell(cellViewClick.point)
        }
    }

    fun animateComputerIconView(pair: Pair<Point, Boolean?>?): Completable {
        if (pair == null) return Completable.error(Throwable("pair == null"))
        val pointCell = pair.first
        return computerIconView.animateComputerIcon(pair.second!!, pointCell, gameBoardViews.getCellViewByPoint(pointCell))
                .doOnEvent { throwable: Throwable? -> checkersViewModel.setMoveOrOptionalPath(pair.first) }
    }

    fun showViews(): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            gameBoardContainer
                    ?.animate()
                    ?.withLayer()
                    ?.alpha(1f)
                    ?.setDuration(200)
                    ?.withEndAction { emitter.onComplete() }
                    ?.start()
        }
                .andThen(playersNamesView.showViewsWithAnimate())
                .andThen(computerIconView.showWithAnimate())
    }

    fun setFinishGame(finishGame: GameFinishData) {
        timerView.dispose()
        startGameDialog.closeDialog()
        val winOrLoose = finishGame.winOrLoose
        textViewTestStart!!.text = winOrLoose
        create().showToast(winOrLoose)
    }

    /**
     * Add the views of the relevant cells start to the list
     *
     * @param cellsViewList list of the relevant cells start
     */
    fun addViewsByRelevantCells(cellsViewList: ImmutableList<DataCellViewClick>?) { // add the relevant cells to the list
        viewsByRelevantCellsList = FluentIterable.from(cellsViewList)
                .transform { obj: DataCellViewClick? -> obj!!.point }
                .transform { point: Point? -> gameBoardViews.getCellViewByPoint(point) }
                .toList()
    }

    /**
     * Clear the views list of the prev relevant cells
     */
    private fun clearPrevRelevantCellsStart() {
        for (cellView in viewsByRelevantCellsList!!) {
            cellView.checked(DataGame.ColorCell.CLEAR_CHECKED, false)
        }
    }

    fun setTimerState(isStartTimer: Boolean) {
        if (isStartTimer) {
            timerView.startTimer()
        } else {
            timerView.stopTimer()
        }
    }

    fun setEndTurn(cellsViewOptionalPath: List<DataCellViewClick>, lastPointPath: Point, pointPawnStartPath: Point, pawnViewStartPath: PawnView): Completable {
        updatePawnViewStart(lastPointPath, pointPawnStartPath, pawnViewStartPath)
        // clear the optional checked path after the turn finished
        clearCheckedCellsPath(cellsViewOptionalPath)
        clearPrevRelevantCellsStart()
        return Completable.complete()
    }

    init {
        findViews(activity)
        gameMode = intent.getIntExtra("GAME_MODE", DataGame.Mode.COMPUTER_GAME_MODE)
        val guestPlayerOne = intent.getSerializableExtra("PLAYER_ONE") as PlayerGame?
        val playerTwoOwner = intent.getSerializableExtra("PLAYER_TWO") as PlayerGame?

        val imageUtil = CheckersImageUtil.create()

        val imageProfilePlayerTwoOwner = imageUtil.createBitmapFromByteArray(playerTwoOwner?.image
                ?: ByteArray(0))
        val imageProfileGuestOrComputer = imageUtil.createBitmapFromByteArray(guestPlayerOne?.image
                ?: ByteArray(0))

        val isShowDialogGame =
                createStartGameDialog(imageProfilePlayerTwoOwner, imageProfileGuestOrComputer, guestPlayerOne, playerTwoOwner)
        createComputerIconView()
        createTitlePlayersViews(
                guestPlayerOne?.playerNme
                , playerTwoOwner?.playerNme
                , isShowDialogGame
                , compositeDisposable
                , imageProfilePlayerTwoOwner
                , imageProfileGuestOrComputer)
        createGameBoardView()
        createTimerView()

        compositeDisposable.add(
                checkersViewModel.totalPawnsChanges
                        .subscribe { totalPawnsDataByPlayer -> playersNamesView.setData(totalPawnsDataByPlayer) }
        )

    }

    private fun createStartGameDialog(
            imageProfilePlayerTwoOwner: Bitmap
            , imageProfileGuestOrComputer: Bitmap
            , guestPlayerOne: PlayerGame?
            , playerTwoOwner: PlayerGame?): Observable<Boolean> {
        startGameDialog = StartGameDialog(
                activity
                , guestPlayerOne!!
                , playerTwoOwner!!
                , gameMode == DataGame.Mode.COMPUTER_GAME_MODE
                , imageProfilePlayerTwoOwner
                , imageProfileGuestOrComputer)
        return startGameDialog.isShowDialogGame()
    }

    private fun createComputerIconView() {
        computerIconView = ComputerIconView(
                activity
                , gameBoardView
                , checkersViewModel.isOwnerAsync)

    }

    private fun createGameBoardView() {
        gameBoardViews = GameBoardViews(gameBoardView, checkersViewModel, activity)
    }

    private fun createTimerView() {
        timerView = ProgressTimeView(activity, checkersViewModel)
    }

    /**
     * Create the avatar, name and the total pawns
     */
    private fun createTitlePlayersViews(
            guestPlayerOneNme: String?
            , playerTwoOwnerNme: String?
            , isShowDialogGame: Observable<Boolean>
            , compositeDisposable: CompositeDisposable
            , imageProfilePlayerTwoOwner: Bitmap
            , imageProfileGuestOrComputer: Bitmap) {
        playersNamesView = PlayersNamesView(
                activity
                , guestPlayerOneNme.toString()
                , playerTwoOwnerNme.toString()
                , isShowDialogGame
                , compositeDisposable
                , imageProfilePlayerTwoOwner
                , imageProfileGuestOrComputer)

    }
}