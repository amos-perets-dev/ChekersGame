package com.example.chekersgamepro.screens.homepage.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import com.example.chekersgamepro.screens.homepage.online.players.OnlinePlayersFragment
import com.example.chekersgamepro.screens.homepage.topplayers.TopPlayersFragment
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.menu_fragment.view.*


class MenuFragment : CheckersFragment() {

//    private val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

    private lateinit var recyclerViewButtons: CheckersRecyclerView

    private lateinit var imageProfile : CircleImageView

    override fun getLayoutResId() = R.layout.menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerViewButtons = view.recycler_view_buttons
        val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

        compositeDisposableOnDestroyed.addAll(
                menuViewModel
                        .openOnlinePlayers(this)
                        .subscribe {startOnlinePlayersFragment()},

                menuViewModel
                        .openComputerGame(this)
                        .subscribe { (activity as HomePageActivity).onClickComputerGame() },

                menuViewModel
                        .openTopPlayers(this)
                        .subscribe { startTopPlayersFragment() },

                menuViewModel
                        .openUpdatePicture(this)
                        .subscribe { (activity as HomePageActivity).onClickAvatar() },

                menuViewModel
                        .openShareGame(this)
                        .subscribe { },

                menuViewModel
                        .openSettings(this)
                        .subscribe { },

                RxView.globalLayouts(this.recyclerViewButtons)
                        .firstOrError()
                        .subscribe { t1, t2 ->
                            val menuButtonsAdapter = MenuButtonsAdapter(menuViewModel.getButtonsList(), this.recyclerViewButtons.measuredHeight)
                            this.recyclerViewButtons.layoutManager = LinearLayoutManager(context)
                            this.recyclerViewButtons.adapter = menuButtonsAdapter
                            this.imageProfile = activity!!.image_profile_hp

                        }
        )
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