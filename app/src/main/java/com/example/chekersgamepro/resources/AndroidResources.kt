package com.example.chekersgamepro.resources

import com.example.chekersgamepro.checkers.CheckersApplication

class AndroidResources : AppResources {

    val context = CheckersApplication.create().applicationContext

    override fun getString(stringResId: Int, vararg args: Any?): String {
        return context.resources.getString(stringResId, args)
    }

    override fun getColor(colorResId: Int): Int {
        return context.resources.getColor(colorResId)
    }
}