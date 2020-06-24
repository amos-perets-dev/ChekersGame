package com.example.chekersgamepro.models.player.data

import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import java.io.Serializable

data class PlayerData(var id: Long = -1
                      , var playerName: String = ""
                      , var avatarEncodeImage: String = ""
                      , var remotePlayerActive: RemotePlayerData = RemotePlayerData()
                      , var owner: Boolean = false
                      , var canPlay: Boolean = false
                      , var technicalLoss: Boolean = false

                      , var requestOnlineGameStatus: RequestOnlineGameStatus = RequestOnlineGameStatus.EMPTY

                      , var userLevel: Int = 1

                      , var nowPlay: Int = PlayersCode.EMPTY.ordinal
                      , var playerCode: Int = PlayersCode.EMPTY.ordinal

                      , var remoteMove: RemoteMove = RemoteMove()
                      , var totalWin: Int = 0
                      , var totalLoss: Int = 0) : Serializable