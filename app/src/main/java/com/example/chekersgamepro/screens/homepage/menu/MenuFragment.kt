package com.example.chekersgamepro.screens.homepage.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.computer.ComputerGameFragment
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import com.example.chekersgamepro.screens.homepage.menu.online.players.PlayersFragment
import com.example.chekersgamepro.screens.homepage.menu.rules.RulesFragment
import com.example.chekersgamepro.screens.homepage.menu.settings.SettingsFragment
import com.example.chekersgamepro.screens.homepage.menu.topplayers.TopPlayersFragment
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.menu_fragment.view.*


class MenuFragment : CheckersFragment() {

    private lateinit var recyclerViewButtons: CheckersRecyclerView

    private lateinit var imageProfile: CircleImageView

    override fun getLayoutResId() = R.layout.menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerViewButtons = view.recycler_view_buttons
        val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

//        view.fab_settings.setOnClickListener(menuViewModel::onClickSettings)
//        view.fab_share.setOnClickListener ( menuViewModel::onClickShare)
//        view.fab_logout.setOnClickListener ( menuViewModel::onClickExit)

        compositeDisposableOnDestroyed.addAll(
                menuViewModel
                        .openOnlinePlayers(this)
                        .subscribe (Functions.actionConsumer(this::startOnlinePlayersFragment)),

                menuViewModel
                        .openComputerGame(this)
                        .subscribe (Functions.actionConsumer(this::startComputerGameFragment)),

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
        createFragment(SettingsFragment(), "settings")

    }

    private fun startRulesFragment() {
        createFragment(RulesFragment(), "rules")
    }

    private fun startComputerGameFragment() {
        createFragment(ComputerGameFragment(), "computer_game")
    }

    private fun startTopPlayersFragment() {
        createFragment(TopPlayersFragment(), "top_players")
    }

    private fun <T : BaseFragment?> createFragment(fragment: T, tag: String){
        if (fragment != null) {
            (fragment as DialogFragment).show(childFragmentManager, tag)
        }
    }

    private fun startOnlinePlayersFragment() {
        createFragment(PlayersFragment(), "online_players")

    }


}