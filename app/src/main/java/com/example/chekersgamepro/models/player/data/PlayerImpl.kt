package com.example.chekersgamepro.models.player.data

import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import java.io.Serializable

open class PlayerImpl(private var id: Long = -1
                      , private var playerName: String = ""
                      , private var avatarEncodeImage: String = ""
//                      , private var remotePlayersQueueList: HashMap<String, RemotePlayerData> = HashMap()
                      , private var remotePlayerActive : RemotePlayerData = RemotePlayerData()
                      , private var owner: Boolean = false
                      , private var canPlay: Boolean = false
                      , private var technicalLoss: Boolean = false

                      , private var requestOnlineGameStatus: Int = RequestOnlineGameStatus.EMPTY.ordinal

                      , private var userLevel: Int = 1

                      , private var nowPlay: Int = PlayersCode.EMPTY.ordinal
                      , private var playerCode: Int = PlayersCode.EMPTY.ordinal

                      , private var remoteMove: RemoteMove = RemoteMove()
                      , private var totalWin : Int = 20
                      , private var totalLoss : Int = 10) : IPlayer, Serializable {

//    override fun addRemotePlayerQueue(remotePlayerQueue: RemotePlayerData) {
////        this.remotePlayersQueueList.add(remotePlayerQueue)
//        this.remotePlayersQueueList[remotePlayerQueue.remotePlayerName] = remotePlayerQueue
//    }
//
//    override fun getRemotePlayerQueue(): List<RemotePlayerData> = this.remotePlayersQueueList.values.toList()


    override fun setRemotePlayerActive(remotePlayerActive: RemotePlayerData) {
        this.remotePlayerActive = remotePlayerActive
    }

    override fun getRemotePlayerActive(): RemotePlayerData = this.remotePlayerActive

    override fun setTotalWin(totalWin: Int) {
        this.totalWin = totalWin
    }

    override fun getTotalWin(): Int  = this.totalWin

    override fun setTotalLoss(totalLoss: Int) {
        this.totalLoss = totalLoss

    }

    override fun getTotalLoss(): Int = this.totalLoss

    override fun getAvatarEncode() = this.avatarEncodeImage

    override fun setAvatarEncode(encodeImage: String) {
        this.avatarEncodeImage = encodeImage
    }

//    override fun getAvatarRemotePlayer(): String = this.remotePlayerActive.remotePlayerAvatar
//
//    override fun setAvatarRemotePlayer(avatarRemotePlayer: String) {
//        this.remotePlayerActive.remotePlayerAvatar = avatarRemotePlayer
//    }

    override fun setIsTechnicalLoss(technicalLoss: Boolean) {
        this.technicalLoss = technicalLoss
    }

    override fun isTechnicalLoss(): Boolean = this.technicalLoss

    override fun setRemoteMove(remoteMove: RemoteMove) {
        this.remoteMove = remoteMove
    }

    override fun getRemoteMove(): RemoteMove = this.remoteMove

    override fun getPlayerCode(): Int = playerCode

    override fun setPlayerCode(playerNameCode: Int) {
        this.playerCode = playerNameCode
    }

    override fun getNowPlay(): Int = nowPlay

    override fun setNowPlay(nowPlay: Int) {
        this.nowPlay = nowPlay
    }

    override fun getPlayerId(): Long = id

    override fun setPlayerId(id: Long) {
        this.id = id
    }

    override fun setRequestOnlineGameStatus(requestOnlineGameStatus: RequestOnlineGameStatus) {
        this.requestOnlineGameStatus = requestOnlineGameStatus.ordinal
    }

    override fun getRequestOnlineGameStatus(): RequestOnlineGameStatus = RequestOnlineGameStatus.values().get(requestOnlineGameStatus)

    override fun getPlayerName(): String = playerName

//    override fun getRemotePlayerName(): String = this.remotePlayerActive.remotePlayerName

    override fun getLevelPlayer(): Int = userLevel

    override fun isOwner(): Boolean = owner

    override fun isCanPlay(): Boolean = canPlay

    override fun setIsOwner(owner: Boolean) {
        this.owner = owner
    }

    override fun setIsCanPlay(canPlay: Boolean) {
        this.canPlay = canPlay
    }

//    override fun setRemotePlayerName(remotePlayerName: String) {
//        this.remotePlayerActive.remotePlayerName = remotePlayerName
//    }

    override fun setPlayerName(playerName: String) {
        this.playerName = playerName
    }

    override fun setLevelUser(userLevel: Int) {
        this.userLevel = userLevel
    }

    override fun toString(): String {
        return "PlayerImpl(id=$id, playerName='$playerName', remotePlayerActive=$remotePlayerActive, owner=$owner, canPlay=$canPlay, technicalLoss=$technicalLoss, requestOnlineGameStatus=$requestOnlineGameStatus, userLevel=$userLevel, nowPlay=$nowPlay, playerCode=$playerCode, remoteMove=$remoteMove, totalWin=$totalWin, totalLoss=$totalLoss)"
    }


}