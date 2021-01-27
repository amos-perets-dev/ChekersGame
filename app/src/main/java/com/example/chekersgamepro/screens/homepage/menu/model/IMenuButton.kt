package com.example.chekersgamepro.screens.homepage.menu.model

import com.example.chekersgamepro.screens.homepage.menu.MenuButtonsType

interface IMenuButton {

    fun getButtonType() : Int

    fun getButtonName() : String

    fun getIcon() : Int

    fun onClick()

}