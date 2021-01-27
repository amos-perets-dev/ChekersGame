package com.example.chekersgamepro.screens.homepage.menu.topplayers

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.top_players_fragment.view.*

class TopPlayersFragment : BaseFragment() {

    private lateinit var recyclerViewTopPlayers: CheckersRecyclerView

    private val topPlayersAdapter = TopPlayersAdapter()

    override fun getTitle() = getString(R.string.activity_home_page_top_players_title_text)

    override fun getLayoutResId(): Int = R.layout.top_players_fragment


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerViewTopPlayers = view.recycler_top_players

        this.compositeDisposableOnDestroyed.addAll(
                TopPlayersInjector()
                        .createViewModelActivity(requireActivity())
                        .getTopPlayersList()
                        .subscribe {
                            this.topPlayersAdapter.updateList(it)
                        },

                RxView.globalLayouts(this.recyclerViewTopPlayers)
                        .subscribeOn(Schedulers.io())
                        .firstOrError()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { t1, t2 ->
                            initRecyclerView()
                        }
        )
    }
    private fun initRecyclerView() {
        this.topPlayersAdapter.setScreenHeight(this.recyclerViewTopPlayers.measuredHeight)
        this.recyclerViewTopPlayers.adapter = topPlayersAdapter
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.isAutoMeasureEnabled = false
        this.recyclerViewTopPlayers.layoutManager = linearLayoutManager
        Log.d("TEST_GAME", "TopPlayersFragment -> initRecyclerView")

    }

    override fun onDestroyView() {
        topPlayersAdapter.dispose()
        super.onDestroyView()
    }
}