package com.example.chekersgamepro.screens.homepage.online.dialog

import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import java.io.Serializable

data class DialogStateCreator(val remotePlayerMsg: IOnlinePlayerEvent
                              , private val isNeedShowMessage: Boolean
                              , private val isNeedShowActionMessage: Boolean
                              , val msgByState: String) : Serializable{
    constructor() : this(OnlinePlayerEventImpl(), false, false, "")

    lateinit var dialogState: DialogState

    init {
        if (!isNeedShowActionMessage && !isNeedShowMessage && msgByState.isEmpty()){
            dialogState = DialogState.WAITING
        } else if (!isNeedShowMessage) {
            dialogState = DialogState.HIDE_MSG
        } else if (isNeedShowMessage) {
            dialogState =
                    if (isNeedShowActionMessage) {
                        DialogState.MSG_WITH_BUTTONS
                    } else {
                        DialogState.MSG_ONLY
                    }
        }

    }

}