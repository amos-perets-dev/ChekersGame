package com.example.chekersgamepro.db.repository.manager

import android.util.Log
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.realm.RealmObject

abstract class BaseManager : Disposable {

    override fun isDisposed(): Boolean {
        Log.d("TEST_GAME", "1 UserProfileManager -> isDisposed")
        return true
    }

    override fun dispose() {
        Log.d("TEST_GAME", "1 UserProfileManager -> dispose")
    }

    abstract  fun <E : RealmObject> insertAsync(`object` : E): Completable
}