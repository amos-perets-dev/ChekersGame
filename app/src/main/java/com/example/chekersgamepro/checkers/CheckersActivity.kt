package com.example.chekersgamepro.checkers

import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

open class CheckersActivity : AppCompatActivity() {

    protected val checkersApplication = CheckersApplication.create()

    protected fun getInteger(resId: Int) = checkersApplication.getInteger(resId)

    protected fun getColorInt(resId: Int) = checkersApplication.getColorRes(resId)

    open fun hideActionBar() {
        val window = window
        window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        actionBar?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideActionBar()
    }
}