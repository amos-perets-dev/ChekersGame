package com.example.chekersgamepro.screens.homepage.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.chekersgamepro.R
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.request_game_dialog.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction0

class RequestGameDialog(context: Context,
                        acceptOnlineGame: KFunction0<Unit>,
                        declineOnlineGame: KFunction0<Unit>,
                        compositeDisposable: CompositeDisposable,
                        msgState: Observable<DialogStateCreator>) {

    private val TIME_TO_CLOSE_DIALOG = 5

    private val dialog = Dialog(context, R.style.DialogWindow)

    private var timer = 0

    private var disposableTimer = Disposables.disposed()

    init {

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.request_game_dialog)

        dialog.setCanceledOnTouchOutside(false)

        setGravityAndAnimate()

        compositeDisposable.addAll(
                RxView.clicks(dialog.accept_online_game_button)
                        .doOnNext { disposableTimer.dispose() }
                        .subscribe { acceptOnlineGame.invoke() },

                RxView.clicks(dialog.decline_online_game_button)
                        .subscribe { declineOnlineGame.invoke() },

                msgState.subscribe(this::setMsgState)

        )

        dialog.setOnShowListener {
            disposableTimer.dispose()
            timer = 0
            disposableTimer = Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { timer++ }
                    .takeUntil { timer == TIME_TO_CLOSE_DIALOG }
                    .filter { timer == TIME_TO_CLOSE_DIALOG }
                    .subscribe { declineOnlineGame.invoke() }
        }

        dialog.setOnDismissListener {
            disposableTimer.dispose()
        }

    }

    private fun setMsgState(dialogStateCreator: DialogStateCreator) {
        val dialogState = dialogStateCreator.dialogState
        val msg = dialogStateCreator.msg
        when (dialogState.ordinal) {
            DialogState.MSG_ONLY.ordinal -> setMsg(msg, View.GONE)
            DialogState.MSG_WITH_BUTTON.ordinal -> setMsg(msg, View.VISIBLE)
            else -> hideMsg()
        }
    }

    private fun setMsg(msg: String, visibilityButtons: Int) {
        if (dialog.online_player_msg_text != null && dialog.accept_online_game_button != null && dialog.decline_online_game_button != null) {
            dialog.online_player_msg_text.text = msg
            dialog.accept_online_game_button.visibility = visibilityButtons
            dialog.decline_online_game_button.visibility = visibilityButtons
            dialog.show()
        }
    }

    private fun hideMsg() {
        dialog.dismiss()
    }

    /**
     * Set the gravity of the dialog
     */
    private fun setGravityAndAnimate() {
        val lp = WindowManager.LayoutParams()
        val window = dialog.window
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val metrics = window.context.resources.displayMetrics
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = (metrics.widthPixels * 1)
        lp.height = (metrics.heightPixels * 0.3).toInt()
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        dialog.window!!.attributes = lp
    }
}