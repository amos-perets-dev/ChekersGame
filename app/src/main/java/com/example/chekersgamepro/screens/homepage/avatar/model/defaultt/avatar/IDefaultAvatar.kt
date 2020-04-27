package com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar

import android.graphics.Bitmap
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface IDefaultAvatar {

    fun getAvatarDefaultImage() : Single<Bitmap>

    fun isSelected() : Observable<Boolean>

    fun isNotSelected() : Boolean

    fun click()

    fun isClearSelected(): Observable<Boolean>

    fun avoidClick() : Observable<Boolean>
}