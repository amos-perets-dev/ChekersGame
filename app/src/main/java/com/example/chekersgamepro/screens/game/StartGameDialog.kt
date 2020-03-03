package com.example.chekersgamepro.screens.game

import android.app.Dialog
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatDialog

import com.example.chekersgamepro.R
import com.example.chekersgamepro.data.data_game.DataGame
import io.reactivex.Completable

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_start_game.*
import java.util.concurrent.TimeUnit

class StartGameDialog(context: Context, intent: Intent){

    /**
     * time in millisecond for the timer
     */
    private val DELAY_TO_CLOSE_REPORT = 2000L

    private val gameMode = PublishSubject.create<Int>()
    private val dialog = Dialog(context)

    init {
        val playerOne = intent.getStringExtra("PLAYER_ONE")
        val playerTwo = intent.getStringExtra("PLAYER_TWO")
        val title = "$playerOne VS $playerTwo"
        val gameModeIntent = intent.getIntExtra("GAME_MODE", DataGame.Mode.OFFLINE_GAME_MODE)


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_start_game)

        dialog.setCanceledOnTouchOutside(false)

        decreaseWindowSize()

        dialog.show_players_title.text = title

        val disposableTimer = Completable.timer(DELAY_TO_CLOSE_REPORT, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnEvent { dialog.dismiss() }
                .subscribe { gameMode.onNext(gameModeIntent) }

        dialog.setOnDismissListener { disposableTimer.dispose()}
    }

    fun getGameMode(): Observable<Int> =  gameMode.hide().doOnSubscribe { dialog.show() }

    private fun decreaseWindowSize() {
        val window = dialog.window
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val metrics = window.context.resources.displayMetrics

        val screenWidth = (metrics.widthPixels * 0.9).toInt()
        val screenHeight = (metrics.heightPixels * 0.2).toInt()

        window.setLayout(screenWidth, screenHeight)
    }
}
