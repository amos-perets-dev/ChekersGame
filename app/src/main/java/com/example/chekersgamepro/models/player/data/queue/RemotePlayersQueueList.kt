package com.example.chekersgamepro.models.player.data.queue

import com.example.chekersgamepro.models.player.data.RemotePlayerData

class RemotePlayersQueueList(){

    private var remotePlayersQueueList: ArrayList<RemotePlayerData> = ArrayList()

    init {
        remotePlayersQueueList.add(RemotePlayerData(1010, "TEST", "TEST"))
    }

    fun addRemotePlayersQueueList(remotePlayerData: RemotePlayerData){
        this.remotePlayersQueueList.add(remotePlayerData)
    }

    fun getRemotePlayerQueue(): List<RemotePlayerData> = this.remotePlayersQueueList

}