package com.example.chekersgamepro.db.repository.manager

import android.util.Log
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.*
import io.reactivex.subjects.PublishSubject
import io.realm.RealmObject

class UserProfileManager(private val realmManager: RealmManager, private val remoteDb: IRemoteDb) : BaseManager() {

     companion object{
         val FACTOR_MONEY = 50
     }

    private val moneyChanges = PublishSubject.create<Pair<Int,Int>>()

    fun getUserProfileDataChanges(): Flowable<UserProfileImpl> =
            realmManager.getUserProfileDataChanges()

    fun getUserProfileMoney() : Single<Int> =
            realmManager.getUserProfileDataChanges()
                    .map { it.getMoney() }
                    .firstOrError()

    fun getImageProfile(): Flowable<String> =
            realmManager.getUserProfileDataChanges()
                    .map { it.getAvatarEncode() }

    fun getEncodeImageProfile(): Flowable<String> =
            realmManager.getUserProfileDataChanges()
                    .map { it.getAvatarEncode() }

    fun getUserProfileMoneyChanges(): Observable<Int> =
            moneyChanges
                    .hide()
                    .distinctUntilChanged()
                    // Check if need to notify the change
                    .filter { money -> money.first != money.second }
                    .flatMap {money ->
                        val oldMoney = money.first
                        val newMoney = money.second

                        val isNeedToAddMoney = newMoney > oldMoney
                        if (isNeedToAddMoney){
                            newMoney - (FACTOR_MONEY * 2)
                        }

                        Observable.just(newMoney)
                    }
                    .doOnNext { t1 -> Log.d("TEST_GAME", "   fun getUserProfileMoneyChanges(): Observable<Int> = -> doOnNext: $t1") }
                    .doOnError { Log.d("TEST_GAME", "  fun getUserProfileMoneyChanges(): Observable<Int> =  -> doOnError: ${it.message}")}



    fun createUser(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable =
            remoteDb.createUser(id, userName, encodeImageDefaultPreUpdate)
                    .cast(UserProfileImpl::class.java)
                    .flatMapCompletable (this::insertAsync)



    fun setMoney(isYourWin: Boolean?): Completable {

        return Single.create<Pair<Int, Int>> {emitter ->

            realmManager.getDefaultRealm().executeTransaction {realm ->

                val userProfile = realm.where(UserProfileImpl::class.java).findFirst()

                val oldMoney = userProfile!!.getMoney()
                val moneyByGameResult = getMoneyByGameStatus(isYourWin, userProfile.getLevelUser(), oldMoney)
                emitter.onSuccess(Pair(oldMoney, moneyByGameResult))
            }
        }
                .flatMapCompletable {money ->
                    remoteDb.setMoney(money.second)
                            .doOnEvent { t1 -> Log.d("TEST_GAME", "remoteDb.setMoney(moneyByGameResult)) -> doOnEvent: ") }
                            .doOnError { Log.d("TEST_GAME", "remoteDb.setMoney(moneyByGameResult) -> doOnError: ${it.message}")}
                            .andThen(realmManager.setMoney(money.second))
                            .doOnEvent { t1 -> Log.d("TEST_GAME", "  .andThen(setMoney(moneyByGameResult)) -> doOnEvent: ") }
                            .doOnError { Log.d("TEST_GAME", "  .andThen(setMoney(moneyByGameResult)) -> doOnError: ${it.message}")}
                            .doOnEvent { moneyChanges.onNext(money) }
                }




    }

    private fun getMoneyByGameStatus(isYourWin: Boolean?, levelUser: Int, userMoney: Int) : Int {
        val moneyStep = (levelUser * FACTOR_MONEY)

        // This is for the state before the game start
        if (isYourWin == null) return (userMoney -moneyStep)

        return if (isYourWin) (userMoney + (moneyStep * 2)) else userMoney
    }

    fun setEncodeImageProfile(encode: String?): Completable {
        return Completable.create {emitter ->

            realmManager.getDefaultRealm().executeTransaction {realm ->

                val userProfileImpl = realm.where(UserProfileImpl::class.java).findFirst()
                userProfileImpl?.setAvatarEncode(encode!!)

                emitter.onComplete()

            }
        }
    }

    override fun <E : RealmObject> insertAsync(`object`: E): Completable  = realmManager.insertAsync(`object` as UserProfileImpl)

}