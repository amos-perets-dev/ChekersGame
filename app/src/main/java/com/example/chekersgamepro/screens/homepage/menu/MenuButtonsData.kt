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
        addButton(MenuButtonsType.COMPUTER_GAME, R.string.activity_home_page_menu_buttons_computer_game_text, 0)
        addButton(MenuButtonsType.ONLINE_PLAYERS, R.string.activity_home_page_menu_buttons_online_game_text, 0)
        addButton(MenuButtonsType.TOP_PLAYERS, R.string.activity_home_page_menu_buttons_top_players_game_text, R.drawable.ic_top_players_icon)
        addButton(MenuButtonsType.SETTINGS, R.string.activity_home_page_menu_buttons_rules_game_text, R.drawable.ic_settings)
        addButton(MenuButtonsType.SHARE, R.string.activity_home_page_menu_buttons_share_game_text, R.drawable.ic_share)
//        addButton(MenuButtonsType.SHARE, R.string.activity_home_page_menu_buttons_share_game_text)
//        addButton(MenuButtonsType.SETTINGS, R.string.activity_home_page_menu_buttons_settings_game_text)
    }

    private fun addButton(menuButtonsType: MenuButtonsType, resId: Int, icon: Int) {
        this.buttonsList.add(MenuButtonImpl(menuButtonsType, getString(resId), this.onClickButton, icon))
    }

    private fun getString(resId: Int) = this.resources.getString(resId)

}