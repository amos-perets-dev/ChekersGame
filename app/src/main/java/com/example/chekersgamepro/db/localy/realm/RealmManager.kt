package com.example.chekersgamepro.db.localy.realm

import com.example.chekersgamepro.models.data.UserDataTmp
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.menu.settings.SettingsData
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
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

    fun getUserProfileDataChanges(): Flowable<UserProfileImpl> =
            realm.where(UserProfileImpl::class.java)
                    .findAllAsync()
                    .asFlowable()
                    .filter { it.isNotEmpty() }
                    .map { it.first()!! }

    fun setUserDataTmp(userDataTmp: UserDataTmp): Completable =
         Completable.create { emitter ->
            this.realm.executeTransaction { realm: Realm ->
                val user = getObjectAsync(realm, UserProfileImpl::class.java)
                user.setMoney(userDataTmp.moneyByGameResult)
                user.setTotalWin(userDataTmp.totalWin)
                user.setTotalLoss(userDataTmp.totalLoss)
                emitter.onComplete()
            }
        }



    fun setUserEncodeImageProfile(encodeImage: String): Completable =
         Completable.create { emitter ->
            emitter.onComplete()

            this.realm.executeTransaction { realm: Realm ->
                getObjectAsync(realm, UserProfileImpl::class.java)
                        .setAvatarEncodeImage(encodeImage)
            }
        }



    fun getSettingsData(): Flowable<SettingsData> =
         realm.where(SettingsData::class.java)
                .findAllAsync()
                .asFlowable()
                .filter { it.isNotEmpty() }
                .map { it.first()!! }

    private fun <E : RealmObject> getObjectAsync(realm: Realm, type: Class<E>) = realm.where(type).findFirstAsync()


}