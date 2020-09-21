package com.example.chekersgamepro.screens.game

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.chekersgamepro.R
import com.example.chekersgamepro.models.player.game.PlayerGame
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.dialog_start_game.*
import java.util.concurrent.TimeUnit

class StartGameDialog(private val activity: Activity
                      , private val guestPlayer: PlayerGame
                      , private val ownerPlayer: PlayerGame
                      , private val isComputerGame: Boolean
                      , private val imageProfilePlayerTwoOwner: Bitmap
                      , private val imageProfileGuestOrComputer: Bitmap) {

    private val limitMoneyGame = guestPlayer.moneyGame + ownerPlayer.moneyGame

    /**
     * time in millisecond for the timer
     */
    private val DELAY_TO_CLOSE_DIALOG = 2000L

    private val DELAY_TO_INCREASE_MONEY_GAME = (DELAY_TO_CLOSE_DIALOG / limitMoneyGame)

    private val ANIMATE_ADD_MONEY_GAME_DURATION = 1800L

    private val ANIMATE_NAME_IMAGE_PROFILE_VS_DURATION = 800L

    private val dialog = Dialog(activity)

    private val isShowDialog = BehaviorSubject.create<Boolean>()

    init {

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_start_game)

        dialog.setCanceledOnTouchOutside(false)

        decreaseWindowSize()

    }

    private val compositeDisposable = CompositeDisposable()

    fun isShowDialogGame(): Observable<Boolean> = isShowDialog.hide()

    private fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun showDialogGame() {
        Log.d("TEST_GAME", "dialog: $dialog")

        dialog.guest_player_computer.text = guestPlayer.playerNme
        dialog.owner_player.text = ownerPlayer.playerNme

        dialog.image_profile_owner_player.setImageBitmap(imageProfilePlayerTwoOwner)
        dialog.image_profile_guest_computer.setImageBitmap(imageProfileGuestOrComputer)

        dialog.show()


        dialog.setOnDismissListener {
            Log.d("TEST_GAME", "StartGameDialog setOnDismissListener")
            compositeDisposable.dispose()
        }
        RxView.globalLayouts(dialog.dialog_start_game)
                .doOnNext {
                    dialog.money_game_count.translationY = dialog.money_game_count.measuredHeight.toFloat() + convertDpToPixel(35f)
                    dialog.center_coin.translationY = dialog.center_coin.measuredHeight.toFloat() + convertDpToPixel(35f)
                }
                .doOnSubscribe { isShowDialog.onNext(true) }
                .distinctUntilChanged()
                .flatMap {
                    animateNameImageProfileAndVs(dialog.guest_player_computer, dialog.image_profile_guest_computer, dialog.vs_icon_text, false)
                            .andThen(animateNameImageProfileAndVs(dialog.owner_player, dialog.image_profile_owner_player, dialog.vs_icon_text, true))
                            .andThen(animateMoneyGameBug(dialog.money_game_count, dialog.center_coin))
                            .andThen(animateAddMoneyByGameState())
                }
                .flatMap {

                    val observable: Observable<*>

                    if (isComputerGame) {
                        observable = Observable.just(true)
                    } else {
                        var count = 1
                        val limitMoneyGame = this.limitMoneyGame + 1
                        observable = Observable.interval(DELAY_TO_INCREASE_MONEY_GAME, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext { dialog.money_game_count.text = count++.toString() }
                                .takeUntil { count == limitMoneyGame }
                                .filter { count == limitMoneyGame }

                    }

                    return@flatMap observable
                            .doOnNext { isShowDialog.onNext(false) }
                            .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                }
                ?.doOnNext { dialog.dismiss() }
                ?.subscribe()?.let {
                    compositeDisposable.add(
                            it
                    )
                }

    }

    private fun animateAddMoneyByGameState(): Observable<Boolean> {
        return if (isComputerGame) {
            Observable.just(true)
        } else {
            animateCoins(dialog.money_bag_guest_computer_player_right, dialog.money_bag_owner_player_left)
        }
    }

    private fun loadAvatar(view: ImageView, url: String, iconPlaceHolder: Int) {
        Glide.with(activity)
                .load(url)
                .placeholder(iconPlaceHolder)
                .into(view)
    }

    public fun closeDialog() {
        dialog.dismiss()
    }

    private fun decreaseWindowSize() {
        val window = dialog.window
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val metrics = window.context.resources.displayMetrics

        val screenWidth = (metrics.widthPixels * 0.9).toInt()

        window.setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun animateCoins(rightView: View, leftView: View): Observable<Boolean> {
        return AnimationUtil.translateWithAlpha(rightView, leftView, dialog.center_coin, ANIMATE_ADD_MONEY_GAME_DURATION)
    }

    private fun animateNameImageProfileAndVs(name: View, imageProfile: View, vs: View, isNeedWaitFinish: Boolean): Completable {
        return AnimationUtil.translateWithRotation(name, imageProfile, vs, isNeedWaitFinish, ANIMATE_NAME_IMAGE_PROFILE_VS_DURATION)
    }

    private fun animateMoneyGameBug(moneyGameText: View, moneyGameIcon: View): Completable {
        return AnimationUtil.translateY(0f, 200, moneyGameText, moneyGameIcon)
    }
}
