package com.example.chekersgamepro.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserProfileImpl(@PrimaryKey private var userName: String = ""
                        , private var money : Int = 0
                        , private var userLevel : Int = 1
                        , private var isRegistered : Boolean = false) : RealmObject(), IUserProfile{

    override fun setUserName(userName: String) {
        this.userName = userName
    }

    override fun setMoney(money: Int)  {
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