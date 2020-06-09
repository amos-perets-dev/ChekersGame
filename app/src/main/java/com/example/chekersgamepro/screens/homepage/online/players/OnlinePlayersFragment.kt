package com.example.chekersgamepro.screens.homepage.online.players

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseFragment
import com.example.chekersgamepro.util.animation.AnimationUtil
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.online_players_fragment.*
import kotlinx.android.synthetic.main.online_players_fragment.view.*
import java.util.concurrent.TimeUnit


class OnlinePlayersFragment(private val imageProfileHp: CircleImageView) : OnlineBaseFragment(imageProfileHp) {

    private val onlinePlayersViewModel by lazy {
        ViewModelProviders.of(this).get(OnlinePlayersViewModel::class.java)
    }

    private lateinit var recyclerViewPlayers: CheckersRecyclerView

    private lateinit var onlinePlayersAdapter: OnlinePlayersAdapter

    private val compositeDisposable = CompositeDisposable()

    companion object {
        fun newInstance(image_profile_hp: CircleImageView): OnlinePlayersFragment? {
            Log.d("TEST_GAME", "OnlinePlayersFragment -> newInstance")
            return OnlinePlayersFragment(image_profile_hp)
        }
    }

    override fun getLayoutResId(): Int = R.layout.online_players_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        this.recyclerViewPlayers = view.recycler_view_players
    }

    @SuppressLint("CheckResult")
    override fun onGlobalLayout() {
        super.onGlobalLayout()

        this.onlinePlayersAdapter = OnlinePlayersAdapter(this.recyclerViewPlayers.measuredWidth, this.recyclerViewPlayers.measuredHeight)

        Log.d("TEST_GAME", "OnlinePlayersFragment -> onGlobalLayout")

        val onlinePlayers = onlinePlayersViewModel.getOnlinePlayers()

        compositeDisposable.add(
                this.onlinePlayersAdapter
                        .getClick()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose { Log.d("TEST_GAME", "1 OnlinePlayersFragment -> doOnDispose") }
                        .subscribe {
                            onlinePlayersViewModel.clickOnPlayerCard(it)
                        }
        )

        compositeDisposable.add(
                onlinePlayers
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose { Log.d("TEST_GAME", "2 OnlinePlayersFragment -> doOnDispose") }
                        .doOnNext(onlinePlayersAdapter::updateList)
                        .subscribe()
        )

        compositeDisposable.add(
                onlinePlayers
                        .observeOn(AndroidSchedulers.mainThread())
                        .firstOrError()
                        .subscribe { t1, t2 ->
                            initRecyclerView()
                        }
        )

        compositeDisposable.add(
                onlinePlayersViewModel
                        .openPlayerDetailsScreen(this)
                        .doOnNext(this::startActivity)
                        .delay(800, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .subscribe(Functions.actionConsumer(this::dismissAllowingStateLoss))
        )
    }

    override fun onDestroyView() {
        Log.d("TEST_GAME", "OnlinePlayersFragment -> onDestroyView")
        this.onlinePlayersAdapter.clear()
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    private fun startActivity(pair: Pair<Intent, ActivityOptionsCompat>) {
        startActivity(pair.first, pair.second!!.toBundle())
    }

    private fun initRecyclerView() {

        this.recyclerViewPlayers.addPagerSnap()
        this.recyclerViewPlayers.adapter = onlinePlayersAdapter
//        val linearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val linearLayoutManager = GridLayoutManager(activity, 2)
        linearLayoutManager.isAutoMeasureEnabled = false
        recycler_view_players.layoutManager = linearLayoutManager
        onlinePlayersAdapter.scrollToPositionWithOffset()

    }
}