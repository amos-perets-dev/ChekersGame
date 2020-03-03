package com.example.chekersgamepro.screens.homepage.dialog

data class DialogStateCreator(val msg: String, val isNeedShowMessage: Boolean, val isNeedShowActionMessage: Boolean) {

    lateinit var dialogState: DialogState

    init {
        if (!isNeedShowMessage) {
            dialogState = DialogState.HIDE_MSG
        } else if (isNeedShowMessage) {
            dialogState =
                    if (isNeedShowActionMessage) {
                        DialogState.MSG_WITH_BUTTON
                    } else {
                        DialogState.MSG_ONLY
                    }
        }
    }

}