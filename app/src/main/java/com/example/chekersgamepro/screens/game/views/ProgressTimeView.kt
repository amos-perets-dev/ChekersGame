package com.example.chekersgamepro.screens.game.views

import android.app.Activity
import android.util.Log
import android.widget.ProgressBar
import com.example.chekersgamepro.screens.game.CheckersViewModel
import com.example.chekersgamepro.checkers.CheckersApplication.Factory.create
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class ProgressTimeView(private val activity: Activity, private val checkersViewModel: CheckersViewModel) {

    private var disposableTimer: Disposable? = null

    private var progressBarTop: ProgressBar = activity.progress_top
    private var progressBarBottom: ProgressBar = activity.progress_bottom
    private var progress = 0


    fun getHeight() = progressBarTop.measuredHeight

    fun startTimer() {
        disposableTimer?.dispose()
        val progressBar: ProgressBar = if (checkersViewModel.isTopPlayer) {
            progressBarTop
        } else {
            progressBarBottom
        }
        disposableTimer = Observable.interval(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { progressBar.progress = progress++ }
                .takeUntil { progress == 3000 }
                .filter { progress == 3000 }
                .doOnNext { Log.d("TEST_GAME", "TIME OVER") }
                .doOnNext { create().showToast("TIME OVER") }
                .subscribe { checkersViewModel.notifyTimeOver() }
    }

    fun stopTimer() {
        progressBarTop.progress = 0
        progressBarBottom.progress = 0
        progress = 0
        disposableTimer?.dispose()
    }

    fun dispose() {
        disposableTimer?.dispose()
    }
}