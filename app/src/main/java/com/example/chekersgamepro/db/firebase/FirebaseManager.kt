package com.example.chekersgamepro.db.firebase

import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase
import com.example.chekersgamepro.models.UserProfileImpl
import com.example.chekersgamepro.util.NetworkUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FirebaseManager {

    private val databaseUsers = FirebaseDatabase.getInstance().getReference("Users")

    private val networkUtil = NetworkUtil()
    init {

    }

    public fun addNewUser(user : UserProfileImpl) : Completable{
        if (!networkUtil.isNetworkAvailable()){
            return Completable.error(Throwable())
        }
        return try {
            databaseUsers.child(user.getUserName()).setValue(user)
            Completable.complete()
        } catch (e : Exception){
            Completable.error(Throwable())
        }
    }

    public fun isUserNameExist(userName : String) : Single<Boolean>{
        val applesQuery = databaseUsers.child(userName)

        return RxFirebaseDatabase.data(applesQuery)
                .map(DataSnapshot::getChildren)
                .map { it.toSet() }
                .map { it.size }
                .flatMap { Single.just(it != 0) }
    }

}