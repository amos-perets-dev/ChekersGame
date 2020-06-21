package com.example.chekersgamepro.db.repository.manager

import android.util.Log
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.models.data.UserDataTmp
import com.example.chekersgamepro.models.user.UserProfileImpl
import io.reactivex.*
import io.reactivex.subjects.PublishSubject
import io.realm.RealmObject

class UserProfileManager(private val realmManager: RealmManager,
                         private val remoteDb: IRemoteDb,
                         private val userProfileDataChanges: Flowable<UserProfileImpl>) : BaseManager() {

    companion object {
        val FACTOR_MONEY = 50
        val MONEY_CHANGE = 555
        val TOTAL_GAMES_CHANGE = 123
    }

    private val realm = realmManager.getDefaultRealm()

    fun getUserProfileLevelChanges() : Flowable<String> =
            this.userProfileDataChanges
                    .map { it.getLevelUser().toString() }

    fun getUserProfileMoney(): Single<String> =
            this.userProfileDataChanges
                    .map { it.getMoney().toString() }
                    .firstOrError()

    fun getUserProfileTotalGames(): Single<UserDataTmp> =
            this.userProfileDataChanges
                    .map { UserDataTmp(it.getTotalLoss(), it.getTotalWin(), 0, 0) }
                    .firstOrError()

    fun getEncodeImageProfile(): Flowable<String> =
            this.userProfileDataChanges
                    .map { it.getAvatarEncode() }

    fun getUserProfileMoneyChanges(): Flowable<String> =
            this.userProfileDataChanges
                    .map { it.getMoney().toString() }

    fun getUserProfileTotalWinChanges(): Flowable<String> =
            realmManager.getUserProfileDataChanges()
                    .map { it.getTotalWin().toString() }

    fun getUserProfileTotalLossChanges(): Flowable<String> =
            realmManager.getUserProfileDataChanges()
                    .map { it.getTotalLoss().toString() }

    fun createUser(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable =
            remoteDb.createUser(id, userName, encodeImageDefaultPreUpdate)
                    .cast(UserProfileImpl::class.java)
                    .flatMapCompletable(this::insertAsync)


    fun setUserDataTmp(isWinAndNeedUpdate: Boolean?): Completable {
        Log.d("TEST_GAME", "3 isInTransaction: ${this.realm.isInTransaction}")

        return Single.create<UserDataTmp> { emitter ->

            realm.executeTransaction { realm ->
                val userProfile = realm.where(UserProfileImpl::class.java).findFirst()

                Log.d("TEST_GAME", "4 isInTransaction: ${this.realm.isInTransaction}")

                val oldMoney = userProfile!!.getMoney()
                var totalLoss = userProfile.getTotalLoss()
                var totalWin = userProfile.getTotalWin()

                if (isWinAndNeedUpdate == null) {
                    totalLoss += 1
                } else if (isWinAndNeedUpdate) {
                    totalWin += 1
                    totalLoss -= 1
                }

                val moneyByGameResult = getMoneyByGameStatus(isWinAndNeedUpdate, userProfile.getLevelUser(), oldMoney)
                emitter.onSuccess(UserDataTmp(totalLoss, totalWin, oldMoney, moneyByGameResult))
            }
        }
                .flatMapCompletable { userDataTmp ->
                    remoteDb.setMoney(userDataTmp.moneyByGameResult)
                            .andThen(remoteDb.setTotalGames(userDataTmp.totalLoss, userDataTmp.totalWin))
                            .andThen(remoteDb.setTopPlayer(userDataTmp.totalWin, userDataTmp.totalLoss, userDataTmp.moneyByGameResult))
                            .andThen(realmManager.setUserDataTmp(userDataTmp))
                }
    }

    private fun getMoneyByGameStatus(isWinAndNeedUpdate: Boolean?, levelUser: Int, userMoney: Int): Int {
        val moneyStep = (levelUser * FACTOR_MONEY)

        // This is for the state before the game start
        if (isWinAndNeedUpdate == null) return (userMoney - moneyStep)

        return if (isWinAndNeedUpdate) (userMoney + (moneyStep * 2)) else userMoney
    }

    fun setEncodeImageProfile(encode: String?): Completable {
        return Completable.create { emitter ->

            realmManager.getDefaultRealm().executeTransaction { realm ->

                val userProfileImpl = realm.where(UserProfileImpl::class.java).findFirst()
                userProfileImpl?.setAvatarEncode(encode!!)

                emitter.onComplete()

            }
        }
    }

    override fun <E : RealmObject> insertAsync(`object`: E): Completable = realmManager.insertAsync(`object` as UserProfileImpl)


}