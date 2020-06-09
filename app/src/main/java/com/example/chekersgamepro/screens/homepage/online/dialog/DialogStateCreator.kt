package com.example.chekersgamepro.screens.homepage.online.dialog

import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import java.io.Serializable

data class DialogStateCreator(val remotePlayerMsg: IOnlinePlayerEvent
                              , val msgByState: String
                              , val status: RequestOnlineGameStatus
                              , private val isOwner: Boolean) : Serializable {
    constructor() : this(OnlinePlayerEventImpl(),"", RequestOnlineGameStatus.EMPTY, false)

    val dialogState = when (status.ordinal) {
        RequestOnlineGameStatus.SEND_REQUEST.ordinal -> DialogState.WAITING
        RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal -> DialogState.MSG_WITH_BUTTONS
        else -> DialogState.MSG_ONLY
    }

}