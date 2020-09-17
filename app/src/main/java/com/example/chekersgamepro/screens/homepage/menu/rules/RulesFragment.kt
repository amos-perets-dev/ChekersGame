package com.example.chekersgamepro.screens.homepage.menu.rules

import android.os.Bundle
import android.text.Html
import android.view.View
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import kotlinx.android.synthetic.main.rules_fragment.view.*

class RulesFragment : BaseFragment() {

    override fun getTitle() = getString(R.string.activity_home_page_rules_game_title_text)

    override fun getLayoutResId(): Int = R.layout.rules_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.text_view_rules_description.text = Html.fromHtml(getString(R.string.activity_home_page_menu_rules_description_text))

    }

}