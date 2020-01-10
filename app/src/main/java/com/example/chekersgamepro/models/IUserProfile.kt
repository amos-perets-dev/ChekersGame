package com.example.chekersgamepro.models

interface IUserProfile {

    fun getUserName() : String

    fun getMoney() : Int

    fun getLevelUser() : Int

    fun isRegistered() : Boolean

    fun setUserName(userName : String)

    fun setMoney(money : Int)

    fun setLevelUser(userLevel : Int)

    fun setIsRegistered(isRegistered : Boolean)
}