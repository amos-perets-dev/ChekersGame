package com.example.chekersgamepro.models.player

import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PlayerImpl(private var id: Long = -1
                      , private var playerName: String = ""
                      , private var remotePlayer: String = ""
                      , private var avatarRemotePlayer: String = ""
                      , private var encodeImage: String = ""

                      , private var owner: Boolean = false
                      , private var canPlay: Boolean = false
                      , private var technicalLoss: Boolean = false

                      , private var requestOnlineGameStatus: Int = RequestOnlineGameStatus.EMPTY.ordinal

                      , private var userLevel: Int = 1

                      , private var nowPlay: Int = PlayersCode.EMPTY.ordinal
                      , private var playerCode: Int = PlayersCode.EMPTY.ordinal

                      , private var remoteMove: RemoteMove = RemoteMove()) : IPlayer {

    override fun getEncodeImage()= this.encodeImage

    override fun setEncodeImage(encodeImage: String) {
        this.encodeImage = encodeImage
    }

    override fun getAvatarRemotePlayer(): String = this.avatarRemotePlayer

    override fun setAvatarRemotePlayer(avatarRemotePlayer: String) {
        this.avatarRemotePlayer = avatarRemotePlayer
    }

    override fun setIsTechnicalLoss(technicalLoss: Boolean) {
        this.technicalLoss = technicalLoss
    }

    override fun isTechnicalLoss(): Boolean  = this.technicalLoss

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

    override fun getRemotePlayer(): String = remotePlayer

    override fun getLevelPlayer(): Int = userLevel

    override fun isOwner(): Boolean = owner

    override fun isCanPlay(): Boolean = canPlay

    override fun setIsOwner(owner: Boolean) {
        this.owner = owner
    }

    override fun setIsCanPlayer(canPlay: Boolean) {
        this.canPlay = canPlay
    }

    override fun setRemotePlayer(remotePlayer: String) {
        this.remotePlayer = remotePlayer
    }

    override fun setPlayerName(playerName: String) {
        this.playerName = playerName
    }

    override fun setLevelUser(userLevel: Int) {
        this.userLevel = userLevel
    }

}