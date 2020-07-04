package com.example.chekersgamepro.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserProfileImpl(@PrimaryKey private var key: String = ""
                           , private var userName: String = " "
                           , private var avatarEncodeImage: String = ""
                           , private var id: Long = -1
                           , private var money: Int = -1
                           , private var userLevel: Int = -1
                           , private var isRegistered: Boolean = false
                           , private var totalWin : Int = -1
                           , private var totalLoss : Int = -1) : RealmObject(), IUserProfile {

    override fun setTotalWin(totalWin: Int) {
        this.totalWin = totalWin
    }

    override fun getTotalWin(): Int  = this.totalWin

    override fun setTotalLoss(totalLoss: Int) {
        this.totalLoss = totalLoss

    }

    override fun getTotalLoss(): Int = this.totalLoss

    override fun getAvatarEncode()= this.avatarEncodeImage

    override fun setAvatarEncodeImage(encodeImage: String) {
        this.avatarEncodeImage = encodeImage
    }

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