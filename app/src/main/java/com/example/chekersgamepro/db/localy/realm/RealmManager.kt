package com.example.chekersgamepro.db.localy.realm

import com.example.chekersgamepro.models.data.UserDataTmp
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.Completable
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmObject
import java.io.Serializable

class RealmManager : Serializable {

    private val realm = Realm.getDefaultInstance()

    fun getDefaultRealm(): Realm {
        return realm
    }


    fun <E : RealmObject> insertAsync(`object`: E): Completable {
        var objectFromRealm: E? = null
        realm.executeTransaction { realm ->
            objectFromRealm = realm.copyToRealmOrUpdate(`object`)
        }

        return if (objectFromRealm == null) Completable.error(Throwable()) else Completable.complete()
    }

    fun getUserProfileDataChanges(): Flowable<UserProfileImpl> {
        return getUserProfileObject()
                .asFlowable<UserProfileImpl>()
                .filter { it.isLoaded }
                .filter { it.isValid }
    }

    fun setUserDataTmp(userDataTmp: UserDataTmp): Completable {
        return Completable.create { emitter ->

            this.realm.executeTransaction { realm: Realm ->
                val user = getUserProfileObject(realm)
                user.setMoney(userDataTmp.moneyByGameResult)
                user.setTotalWin(userDataTmp.totalWin)
                user.setTotalLoss(userDataTmp.totalLoss)
                emitter.onComplete()
            }
        }
    }

    private fun getUserProfileObject(realm: Realm = this.realm) = getObjectByClass(UserProfileImpl::class.java, realm)

    private fun <E : RealmObject> getObjectByClass(type: Class<E>, realm: Realm = this.realm) = realm.where(type).findFirstAsync()

}