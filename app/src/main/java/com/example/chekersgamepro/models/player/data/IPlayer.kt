package com.example.chekersgamepro.models.player.data

import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus

interface IPlayer {

    fun getPlayerId() : Long
    fun setPlayerId(id : Long)

    fun getPlayerName() : String
    fun setPlayerName(playerName : String)

    fun getLevelPlayer() : Int
    fun setLevelUser(userLevel : Int)

    fun isOwner() : Boolean
    fun setIsOwner(owner : Boolean)

    fun isCanPlay() : Boolean
    fun setIsCanPlay(canPlay : Boolean)

//    fun getRemotePlayerName() : String
//    fun setRemotePlayerName(remotePlayer : String)

//    fun getAvatarRemotePlayer() : String
//    fun setAvatarRemotePlayer(avatarRemotePlayer : String)

    fun setRequestOnlineGameStatus(requestOnlineGameStatus : RequestOnlineGameStatus)
    fun getRequestOnlineGameStatus() : RequestOnlineGameStatus

    fun getPlayerCode() : Int
    fun setPlayerCode(playerNameCode: Int)

    fun setNowPlay(nowPlay: Int)
    fun getNowPlay(): Int

    fun setRemoteMove(remoteMove : RemoteMove)
    fun getRemoteMove(): RemoteMove

    fun setIsTechnicalLoss(technicalLoss : Boolean)
    fun isTechnicalLoss(): Boolean

    fun getAvatarEncode() : String
    fun setAvatarEncode(encodeImage : String)

    fun setTotalWin(totalWin: Int)
    fun getTotalWin(): Int


    fun setTotalLoss(totalLoss: Int)
    fun getTotalLoss(): Int

//    fun addRemotePlayerQueue(remotePlayerQueue: RemotePlayerData)
//    fun getRemotePlayerQueue(): List<RemotePlayerData>

    fun setRemotePlayerActive(remotePlayerActive: RemotePlayerData)
    fun getRemotePlayerActive(): RemotePlayerData

}