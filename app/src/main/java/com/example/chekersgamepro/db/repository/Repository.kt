package com.example.chekersgamepro.db.repository

import android.graphics.Bitmap
import com.example.chekersgamepro.models.player.data.PlayerData
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer
import com.example.chekersgamepro.screens.registration.RegistrationStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface Repository {
    fun addNewUser(userName: String): Single<RegistrationStatus>
    fun storeImage(/*newImageProfile: Bitmap?*/): Completable/*: Completable*/
    fun setImageProfileTmp(image: Bitmap?)
    fun getImageProfileTmp() : String?
    fun getRemotePlayerById(playerId: Long): PlayerData
    fun technicalFinishGamePlayer() : Completable
    fun getTopPlayersList() : Observable<List<ITopPlayer>>


}