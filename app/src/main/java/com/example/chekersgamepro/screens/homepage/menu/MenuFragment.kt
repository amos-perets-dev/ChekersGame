package com.example.chekersgamepro.screens.homepage.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import com.example.chekersgamepro.screens.homepage.menu.online.players.OnlinePlayersFragment
import com.example.chekersgamepro.screens.homepage.menu.rules.RulesFragment
import com.example.chekersgamepro.screens.homepage.menu.settings.SettingsFragment
import com.example.chekersgamepro.screens.homepage.menu.topplayers.TopPlayersFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.menu_fragment.view.*
import kotlin.reflect.KFunction1


class MenuFragment : CheckersFragment() {

    private lateinit var recyclerViewButtons: CheckersRecyclerView

    private lateinit var imageProfile: CircleImageView

    override fun getLayoutResId() = R.layout.menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerViewButtons = view.recycler_view_buttons
        val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

        view.fab_settings.setOnClickListener(menuViewModel::onClickSettings)
        view.fab_share.setOnClickListener ( menuViewModel::onClickShare)
        view.fab_logout.setOnClickListener ( menuViewModel::onClickExit)

        compositeDisposableOnDestroyed.addAll(
                menuViewModel
                        .openOnlinePlayers(this)
                        .subscribe (Functions.actionConsumer(this::startOnlinePlayersFragment)),

                menuViewModel
                        .openComputerGame(this)
                        .subscribe (Functions.actionConsumer(menuViewModel::onClickComputerGame)),

                menuViewModel
                        .openTopPlayers(this)
                        .subscribe (Functions.actionConsumer(this::startTopPlayersFragment)),

                menuViewModel
                        .openUpdatePicture(this)
                        .subscribe (Functions.actionConsumer(this::startRulesFragment)),

                menuViewModel
                        .openSettings(this)
                        .subscribe (Functions.actionConsumer(this::startSettingsFragment)) ,

                menuViewModel
                        .shareApp(this)
                        .subscribe (this::startActivity),

                menuViewModel
                        .isCloseApp(this)
                        .subscribe (),

                menuViewModel
                        .startComputerGame(this)
                        .subscribe (this::startComputerGame),

                        RxView.globalLayouts (this.recyclerViewButtons)
                        .firstOrError()
                        .subscribe { t1, t2 ->
                            val menuButtonsAdapter = MenuButtonsAdapter(menuViewModel.getButtonsList(), this.recyclerViewButtons.measuredHeight)
                            this.recyclerViewButtons.layoutManager = LinearLayoutManager(context)
                            this.recyclerViewButtons.adapter = menuButtonsAdapter
                            this.imageProfile = activity!!.image_profile_hp

                        }
        )
    }

    private fun startComputerGame(intent : Intent){
        startActivityForResult(intent, 55)
    }

    private fun startSettingsFragment() {
        val settingsFragment: Fragment? = SettingsFragment()
        if (settingsFragment != null) {
            (settingsFragment as DialogFragment).show(childFragmentManager, "settings")
        }
    }

    private fun startRulesFragment() {
        val rulesFragment: Fragment? = RulesFragment()
        if (rulesFragment != null) {
            (rulesFragment as DialogFragment).show(childFragmentManager, "rules")
        }
    }


    private fun startTopPlayersFragment() {
        val topPlayersFragment: Fragment? = TopPlayersFragment()
        if (topPlayersFragment != null) {
            (topPlayersFragment as DialogFragment).show(childFragmentManager, "top_players")
        }
    }

    private fun startOnlinePlayersFragment() {
        val onlinePlayersFragment: Fragment? = OnlinePlayersFragment()
        if (onlinePlayersFragment != null) {
            (onlinePlayersFragment as DialogFragment).show(childFragmentManager, "online_players")
        }
    }


}