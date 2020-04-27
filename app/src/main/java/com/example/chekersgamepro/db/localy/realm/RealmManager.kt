package com.example.chekersgamepro.db.localy.realm

import android.util.Log
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.PlayerImpl
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
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
//
//    fun getPlayerDataChanges(): Flowable<PlayerImpl> {
//        return getPlayerObject()
//                .asFlowable<PlayerImpl>()
//                .filter { it.isLoaded }
//                .filter { it.isValid }
//    }
//
//    fun setRemoteMove(remoteMove: RemoteMove): Completable {
//        return Completable.create { emitter ->
//            realm.executeTransaction { realm: Realm ->
//                getPlayerObject(realm)?.setRemoteMove(realm.copyToRealm(remoteMove))
//                emitter.onComplete()
//            }
//        }
//                .doOnEvent {
//                    Log.d("TEST_GAME", "45676543 PlayerManager -> after  .andThen(realmManager.getPlayerAsync()) doOnEvent: ${it?.message}")
//                }
//                .doOnError {
//                    Log.d("TEST_GAME", "5346346 PlayerManager -> after  .andThen(realmManager.getPlayerAsync()) error: ${it.message}")
//                }
//
//    }


//    fun setPlayerIsCanPlay(isCanPlay: Boolean): Completable {
//        return Completable.create { emitter ->
//
//            realm.executeTransaction { realm: Realm ->
//                getPlayerObject(realm)?.setIsCanPlayer(isCanPlay)
//                emitter.onComplete()
//            }
//        }
//                .doOnEvent {
//                    Log.d("TEST_GAME", "45676543 PlayerManager -> after  .andThen(realmManager.getPlayerAsync()) doOnEvent: ${it?.message}")
//                }
//                .doOnError {
//                    Log.d("TEST_GAME", "5346346 PlayerManager -> after  .andThen(realmManager.getPlayerAsync()) error: ${it.message}")
//                }
//
//    }

    fun setMoney(money: Int): Completable {
        return Completable.create { emitter ->

            realm.executeTransaction { realm: Realm ->
                getUserProfileObject(realm)?.setMoney(money)
                emitter.onComplete()
            }
        }
                .doOnEvent {
                    Log.d("TEST_GAME", "45676543 PlayerManager -> after    fun setMoney(money: Int): Completable {) doOnEvent: ${it?.message}")
                }
                .doOnError {
                    Log.d("TEST_GAME", "5346346 PlayerManager -> after    fun setMoney(money: Int): Completable { error: ${it.message}")
                }

    }

//    fun getPlayerAsync(): Single<PlayerImpl> = Single.just(realm.where(PlayerImpl::class.java).findFirst()!!)

//    private fun getPlayerObject(realm: Realm = this.realm) = getObjectByClass(PlayerImpl::class.java, realm)
    private fun getUserProfileObject(realm: Realm = this.realm) = getObjectByClass(UserProfileImpl::class.java, realm)

    private fun <E : RealmObject> getObjectByClass(type: Class<E>, realm: Realm = this.realm) = realm.where(type).findFirstAsync()

}