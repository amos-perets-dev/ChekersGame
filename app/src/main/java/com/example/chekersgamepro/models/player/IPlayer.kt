package com.example.chekersgamepro.models.player

import com.example.chekersgamepro.data.move.Move
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
    fun setIsCanPlayer(canPlay : Boolean)

    fun getRemotePlayer() : String
    fun setRemotePlayer(remotePlayer : String)

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
}