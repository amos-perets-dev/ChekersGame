package com.example.chekersgamepro.models.player.data

data class RemotePlayerData(var remotePlayerId: Long = -1
                            , var remotePlayerAvatar: String = ""
                            , var remotePlayerName: String = ""){
    constructor() : this(-1, "", "")

    override fun toString(): String {
        return "RemotePlayerData(remotePlayerId=$remotePlayerId, remotePlayerName='$remotePlayerName')"
    }

}