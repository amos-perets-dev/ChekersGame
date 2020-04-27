package com.example.chekersgamepro.db.repository

import android.graphics.Bitmap
import com.example.chekersgamepro.screens.registration.RegistrationStatus
import io.reactivex.Completable
import io.reactivex.Single

interface Repository {
    fun addNewUser(userName: String): Single<RegistrationStatus>
    fun storeImage(/*newImageProfile: Bitmap?*/): Completable/*: Completable*/
    fun setImageProfileTmp(image: Bitmap?)
    fun getImageProfileTmp() : String?


}