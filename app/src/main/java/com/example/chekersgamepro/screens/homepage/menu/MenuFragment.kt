package com.example.chekersgamepro.screens.homepage.menu

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.menu_fragment.view.*

class MenuFragment : CheckersFragment() {

//    private val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

    private lateinit var recyclerViewButtons: CheckersRecyclerView

    override fun getLayoutResId() =  R.layout.menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.recyclerViewButtons = view.recycler_view_buttons
        val menuViewModel = MenuInjector().createViewModelActivity(activity!!)

        compositeDisposableOnDestroyed.addAll(
                menuViewModel
                        .openOnlineGame(this)
                        .subscribe { (activity as HomePageActivity).onClickOnlineGame() },

                menuViewModel
                        .openComputerGame(this)
                        .subscribe { (activity as HomePageActivity).onClickComputerGame() },

                menuViewModel
                        .openTopPlayers(this)
                        .subscribe { (activity as HomePageActivity).onClickTopPlayers() },

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
                        }
        )
    }

}