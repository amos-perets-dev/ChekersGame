package com.example.chekersgamepro.util.keybord_util

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class KeyboardUtil constructor(private var rootView : View): ViewTreeObserver.OnGlobalLayoutListener {

    private val publishSubjectOpenKeyboard = PublishSubject.create<KeyboardUtilData>()

    init {
        this.rootView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {

        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val screenHeight = rootView.rootView.height

        // r.bottom is the position above soft keypad or device button.
        // if keypad is shown, the r.bottom is smaller than that before.
        val keypadHeight = screenHeight - r.bottom
        Log.d("TEST TEST", "keypadHeight: $keypadHeight")
        publishSubjectOpenKeyboard.onNext(KeyboardUtilData(keypadHeight, keypadHeight > screenHeight * 0.15))
    }

    fun getObservableKeyboardOpen() : Observable<KeyboardUtilData>{
        return publishSubjectOpenKeyboard.hide()
                .startWith(KeyboardUtilData(0, false))
                .distinctUntilChanged()
    }

}