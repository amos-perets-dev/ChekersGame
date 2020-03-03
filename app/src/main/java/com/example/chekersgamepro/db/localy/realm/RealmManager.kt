package com.example.chekersgamepro.db.localy.realm

import android.util.Log
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import java.io.Serializable

class RealmManager : Serializable {

    private val realm = Realm.getDefaultInstance()

    fun getDefaultRealm() : Realm {
        return realm
    }


    fun <E : RealmObject> insertAsync(`object`: E): Completable {
        var objectFromRealm: E? = null
        realm.executeTransaction{ realm ->
            objectFromRealm = realm.copyToRealmOrUpdate(`object`)
        }

        return if (objectFromRealm == null) Completable.error(Throwable()) else Completable.complete()
    }

    fun getUserProfileDataChanges(value: String): Flowable<UserProfileImpl> {
        return realm
                .where(UserProfileImpl::class.java)
                .equalTo("userName", value)
                .findFirstAsync()  // maybe use findAllAsync instead?
                .asFlowable<UserProfileImpl>()
                .filter{ it.isLoaded }
                .filter{ it.isValid }
                .doOnNext {
                    Log.d("TEST_GAME", "RealmManager -> MONEY: ${it.getMoney()}")
                }
    }



}