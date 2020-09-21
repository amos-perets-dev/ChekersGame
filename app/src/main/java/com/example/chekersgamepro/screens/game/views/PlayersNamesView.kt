package com.example.chekersgamepro.screens.game.views

import android.app.Activity
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.example.chekersgamepro.data.pawn.total.TotalPawnsDataByPlayer
import com.example.chekersgamepro.views.custom.Typewriter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.activity_main.*

class PlayersNamesView(private val activity: Activity,
                       private val playerOne: String,
                       private val playerTwo: String,
                       private val isShowDialogGame: Observable<Boolean>,
                       private val compositeDisposable: CompositeDisposable,
                       private val imageProfilePlayerTwoOwner: Bitmap,
                       private val imageProfileGuestOrComputer: Bitmap) {

    private val playerOneName: Typewriter = activity.text_view_name_computer_guest
    private val playerTwoName: Typewriter = activity.text_view_name_owner_player
    private val totalPawnsPlayerOne: AppCompatTextView = activity.text_view_total_pawns_computer_guest
    private val totalPawnsPlayerTwo: AppCompatTextView = activity.text_view_total_pawns_owner_player
    private val imageProfileGuestComputerScreen = activity.image_profile_guest_computer_screen
    private val imageProfileOwnerPlayerScreen = activity.image_profile_owner_player_screen
    private val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    private val widthPixels = displayMetrics.widthPixels
    private val heightPixels = displayMetrics.heightPixels

    init {

        animateAvatarView(imageProfileOwnerPlayerScreen, false)
        animateAvatarView(imageProfileGuestComputerScreen, false, isBottom = false)

        imageProfileOwnerPlayerScreen.setImageBitmap(imageProfilePlayerTwoOwner)
        imageProfileGuestComputerScreen.setImageBitmap(imageProfileGuestOrComputer)

        isShowDialogGame
                .filter(Functions.equalsWith(false))
                ?.subscribe {
                    imageProfileOwnerPlayerScreen.alpha = 1F
                    imageProfileGuestComputerScreen.alpha = 1F

                    animateAvatarView(imageProfileOwnerPlayerScreen, true, nextAnimateView = totalPawnsPlayerOne, playNameView = playerOneName, playerName = playerOne)
                    animateAvatarView(imageProfileGuestComputerScreen, true, nextAnimateView = totalPawnsPlayerTwo, playNameView = playerTwoName, playerName = playerTwo)
                }?.let {
                    compositeDisposable.add(
                            it
                    )
                }


    }

    private fun animateAvatarView(
            view: View
            , isReturn: Boolean
            , isBottom: Boolean = true
            , nextAnimateView: View? = null
            , playNameView: Typewriter? = null
            , playerName: String = "") {
        val height = (heightPixels / 2).toFloat()

        view
                .animate()
                .translationX(if (isReturn) 0F else widthPixels / 2.toFloat())
                .translationY(if (isReturn) 0F else if (isBottom) -height else height)
                .setDuration(if (isReturn) 1500 else 1)
                .withEndAction { animateTotalPawnsView(nextAnimateView, playNameView, playerName) }
                .start()
    }

    private fun animateTotalPawnsView(nextAnimateView: View?, playNameView: Typewriter?, playerName: String) {
        if (nextAnimateView != null) {
            nextAnimateView.alpha = 1F
            nextAnimateView
                    .animate()
                    .translationX(0F)
                    .translationY(0F)
                    .setDuration(250)
                    .withEndAction {
                        playNameView?.setCharacterDelay(150)
                        playNameView?.animateText(playerName)
                    }
                    .start()
        }
    }

    private fun initPlayersName() {
        setPlayersText()
        setPlayersNameTranslationX()
        setPlayerTurn(true)
//        playerOneName.invalidate()
//        playerTwoName.invalidate()
    }

    private fun setPlayersNameTranslationX() {
        val translationX = activity.windowManager.defaultDisplay.width / 2
//        playerOneName.translationX = -translationX.toFloat()
//        playerTwoName.translationX = -translationX.toFloat()
//        totalPawnsPlayerOne.translationX = translationX.toFloat()
//        totalPawnsPlayerTwo.translationX = translationX.toFloat()
    }

    private fun setPlayersText() {
//        playerOneName.text = playerOne
//        playerTwoName.text = playerTwo
    }

    fun setPlayerTurn(isPlayerOneTurn: Boolean) { //        if (isPlayerOneTurn) {
//            playerOneName.setIsTurn(true);
//            playerTwoName.setIsTurn(false);
//        } else {
//            playerTwoName.setIsTurn(true);
//            playerOneName.setIsTurn(false);
//        }
    }

    fun showViewsWithAnimate(): Completable {
//        animateTextView(playerOneName)
//        animateTextView(playerTwoName)
//        animateTextView(totalPawnsPlayerOne)
//        animateTextView(totalPawnsPlayerTwo)
        return Completable.complete()
    }

    private fun animateTextView(view: View) {
        view.animate().withLayer().withStartAction { view.alpha = 1f }.translationX(0f).setDuration(500).start()
    }

    fun setData(totalPawnsDataByPlayer: TotalPawnsDataByPlayer) {
        totalPawnsPlayerOne.text = totalPawnsDataByPlayer.totalPawnsPlayerOne
        totalPawnsPlayerTwo.text = totalPawnsDataByPlayer.totalPawnsPlayerTwo
    }

    init {
        initPlayersName()
    }
}