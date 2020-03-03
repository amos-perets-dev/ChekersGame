package com.example.chekersgamepro.models.user

import android.util.Log
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserProfileImpl(@PrimaryKey private var key: String = ""
                           , private var userName: String = " "
                           , private var id: Long = -1
                           , private var money: Int = 0
                           , private var userLevel: Int = 1
                           , private var isRegistered: Boolean = false) : RealmObject(), IUserProfile {
    override fun setKey(key: String) {
        this.key = key
    }

    override fun getKey(): String = key

    override fun getUserId(): Long = id

    override fun setUserId(id: Long) {
        this.id = id
    }

    override fun setUserName(userName: String) {
        this.userName = userName
    }

    override fun setMoney(money: Int)  {
        Log.d("TEST_GAME", "UserProfileImpl -> setMoney")

        this.money = money

    }

    override fun setLevelUser(userLevel: Int) {
        this.userLevel = userLevel

    }

    override fun setIsRegistered(isRegistered: Boolean) {
        this.isRegistered = isRegistered

    }

    override fun isRegistered(): Boolean = isRegistered

    override fun getUserName(): String = userName

    override fun getMoney(): Int = money

    override fun getLevelUser(): Int = userLevel
}