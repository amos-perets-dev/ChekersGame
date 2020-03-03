package com.example.chekersgamepro.db.repository.manager

import android.util.Log
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class UserProfileManager(private val realmManager: RealmManager, private val remoteDb: IRemoteDb) : Disposable {

    private lateinit var userProfile: IUserProfile

    private val FACTOR_MONEY = 50

    fun getUserProfileDataChanges(userName: String): Flowable<UserProfileImpl> =
            realmManager.getUserProfileDataChanges(userName)
                    .doOnNext {userProfile ->
                        this.userProfile = userProfile
                        Log.d("TEST_GAME", "UserProfileManager -> MONEY: ${userProfile.getMoney()}")
                    }

    fun createUser(id: Long, userName: String): Single<IUserProfile> {
        return remoteDb.createUser(id, userName)
                .doOnEvent { user, throwable ->
                    if (throwable == null) {
                        this.userProfile = user
                    }
                }
    }

    fun insertAsync(): Completable {
        return realmManager.insertAsync(userProfile as UserProfileImpl)
    }

    fun setMoney(isYourWin: Boolean?): Completable {

            Log.d("TEST_GAME", "UserProfileManager -> setMoney userProfile.getMoney(): ${userProfile.getMoney()}")

        val moneyByGameResult = getMoneyByGameResult(isYourWin, userProfile.getLevelUser(), userProfile.getMoney())

        return remoteDb.setMoney(moneyByGameResult)
                .andThen(setMoney(moneyByGameResult))
    }

    private fun setMoney(money: Int) : Completable{
        Log.d("TEST_GAME", "1 UserProfileManager -> setMoney: $money")

        return Completable.create {emitter ->
            Log.d("TEST_GAME", "2 UserProfileManager -> setMoney: $money")

            realmManager.getDefaultRealm().executeTransaction {realm ->
                Log.d("TEST_GAME", "3 UserProfileManager -> setMoney: $money")

                val userProfileImpl = realm.where(UserProfileImpl::class.java).findFirst()
                userProfileImpl?.setMoney(money)

                val userProfileImpl1 = realm.where(UserProfileImpl::class.java).findFirst()

                Log.d("TEST_GAME", "55 UserProfileManager -> setMoney: ${userProfileImpl1?.getMoney()}")

                emitter.onComplete()

            }
        }
    }

    private fun getMoneyByGameResult(isYourWin: Boolean?, levelUser: Int, userMoney: Int) : Int {
        val moneyStep = (levelUser * FACTOR_MONEY)

        // This for the state of the pre start game
        if (isYourWin == null) return (userMoney -moneyStep)

        return if (isYourWin) (userMoney + (moneyStep * 2)) else userMoney
    }

    override fun isDisposed(): Boolean {
        Log.d("TEST_GAME", "1 UserProfileManager -> isDisposed")
        return true
    }

    override fun dispose() {
        Log.d("TEST_GAME", "1 UserProfileManager -> dispose")
    }
}