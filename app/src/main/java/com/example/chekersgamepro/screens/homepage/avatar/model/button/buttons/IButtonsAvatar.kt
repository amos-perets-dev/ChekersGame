package com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons

import com.example.chekersgamepro.screens.homepage.avatar.model.button.button.IButtonAvatarSelected
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import io.reactivex.Observable
import io.reactivex.Single

interface IButtonsAvatar {

    fun getButtonClick(): Observable<Int>

    fun createButtonAvatarSelectedList(infoScrollPageData: Observable<ScrollPageData>): Single<ArrayList<IButtonAvatarSelected>>

}