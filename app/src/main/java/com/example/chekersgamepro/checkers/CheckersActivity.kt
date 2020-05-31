package com.example.chekersgamepro.checkers

import androidx.appcompat.app.AppCompatActivity

open class CheckersActivity : AppCompatActivity() {

    protected val checkersApplication = CheckersApplication.create()

    protected fun getInteger(resId: Int) = checkersApplication.getInteger(resId)

    protected fun getColorInt(resId: Int) = checkersApplication.getColorRes(resId)
}