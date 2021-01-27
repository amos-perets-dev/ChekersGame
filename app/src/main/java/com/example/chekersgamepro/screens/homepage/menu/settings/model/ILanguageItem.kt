package com.example.chekersgamepro.screens.homepage.menu.settings.model

import io.reactivex.Observable

interface ILanguageItem {

    fun getName() : String

    fun getPosition() : Int

    fun setPosition(position : Int)

    fun getIcon() : Int

    fun isSelected() : Observable<Boolean>?

    fun onClickItem(adapterPosition: Int)
}