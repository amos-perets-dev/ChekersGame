package com.example.chekersgamepro.resources

interface AppResources {
    fun getString(stringResId: Int, vararg args: Any?): String

    fun getColor(colorResId: Int): Int
}