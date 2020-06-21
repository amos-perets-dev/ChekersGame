package com.example.chekersgamepro.screens.homepage.menu.model

import android.util.Log
import com.example.chekersgamepro.screens.homepage.menu.MenuButtonsType
import io.reactivex.subjects.PublishSubject

class MenuButtonImpl(private val menuButtonsType: MenuButtonsType,
                     private val buttonName: String,
                     private val onClickButton: PublishSubject<Int>) : IMenuButton {

    override fun getButtonType() = this.menuButtonsType.ordinal

    override fun getButtonName() = this.buttonName

    override fun onClick() {
        Log.d("TEST_GAME", "onClick: STATE: ${this.menuButtonsType.name}")
        onClickButton.onNext(this.menuButtonsType.ordinal)
    }

}