package com.example.chekersgamepro.screens.homepage.topplayers

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseFragment
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.top_players_fragment.view.*

class TopPlayersFragment(imageProfileHp: CircleImageView) : OnlineBaseFragment(imageProfileHp) {

    private lateinit var recyclerViewTopPlayers: CheckersRecyclerView

    private val topPlayersAdapter = TopPlayersAdapter()

    companion object {
        fun newInstance(image_profile_hp: CircleImageView): TopPlayersFragment? {
            Log.d("TEST_GAME", "TopPlayersFragment -> newInstance")
            return TopPlayersFragment(image_profile_hp)
        }
    }

    override fun getTitle() = getString(R.string.activity_home_page_top_players_title_text)

    override fun getLayoutResId(): Int = R.layout.top_players_fragment


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerViewTopPlayers = view.recycler_top_players

        val topPlayersViewModel = TopPlayersInjector().createViewModelActivity(activity!!)

        compositeDisposableOnDestroyed.addAll(
                topPlayersViewModel
                        .getTopPlayersList()
                        .observeOn(AndroidSchedulers.mainThread())
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
        this.recyclerViewTopPlayers.layoutManager = LinearLayoutManager(activity)

    }
}