package com.example.chekersgamepro.screens.homepage.avatar.model.button.button

import io.reactivex.Observable

interface IButtonAvatarSelected {

    fun getDrawbleResId() : Int

    fun isSelected() : Observable<Boolean>

    fun getId() : Int

    fun isUnSelected() : Observable<Boolean>

    fun isStartsSelected() : Boolean
    fun onClick()
}