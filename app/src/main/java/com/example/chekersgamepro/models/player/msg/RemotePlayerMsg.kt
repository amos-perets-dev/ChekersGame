package com.example.chekersgamepro.models.player.msg

import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl

class RemotePlayerMsg( var remoteOnlinePlayer: IOnlinePlayerEvent,  var msg : String) {
}