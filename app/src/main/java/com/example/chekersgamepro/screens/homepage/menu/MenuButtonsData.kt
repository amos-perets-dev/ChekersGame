package com.example.chekersgamepro.screens.homepage.menu

import android.content.res.Resources
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton
import com.example.chekersgamepro.screens.homepage.menu.model.MenuButtonImpl
import io.reactivex.subjects.PublishSubject

class MenuButtonsData(private val resources: Resources,
                      private val onClickButton: PublishSubject<Int>) {

    val buttonsList = ArrayList<IMenuButton>()

    init {
        addButton(MenuButtonsType.ONLINE_PLAYERS, R.string.activity_home_page_menu_buttons_online_game_text)
        addButton(MenuButtonsType.COMPUTER_GAME, R.string.activity_home_page_menu_buttons_computer_game_text)
        addButton(MenuButtonsType.TOP_PLAYERS, R.string.activity_home_page_menu_buttons_top_players_game_text)
        addButton(MenuButtonsType.UPDATE_PICTURE, R.string.activity_home_page_menu_buttons_update_picture_game_text)
        addButton(MenuButtonsType.SHARE, R.string.activity_home_page_menu_buttons_share_game_text)
        addButton(MenuButtonsType.SETTINGS, R.string.activity_home_page_menu_buttons_settings_game_text)
    }

    private fun addButton(menuButtonsType : MenuButtonsType, resId : Int){
        this.buttonsList.add(MenuButtonImpl(menuButtonsType, getString(resId), this.onClickButton))
    }

    private fun getString(resId : Int) = this.resources.getString(resId)

}