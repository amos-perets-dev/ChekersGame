package com.example.chekersgamepro.models.user

interface IUserProfile {

    fun getUserName() : String

    fun getUserId() : Long

    fun getMoney() : Int

    fun getLevelUser() : Int

    fun isRegistered() : Boolean

    fun setKey(key : String)

    fun getKey() : String

    fun setUserName(userName : String)

    fun setMoney(money : Int)

    fun setLevelUser(userLevel : Int)

    fun setIsRegistered(isRegistered : Boolean)

    fun setUserId(id : Long)

}