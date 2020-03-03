package com.example.chekersgamepro.screens.homepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.chekersgamepro.R
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.screens.homepage.dialog.RequestGameDialog
import com.example.chekersgamepro.screens.homepage.recyclerview.OnlinePlayersAdapter
import com.example.chekersgamepro.util.CheckersApplication
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.activity_home_page.*
import java.util.concurrent.TimeUnit

class HomePageActivity : AppCompatActivity() {

    private val homePageViewModel by lazy {
        ViewModelProviders.of(this).get(HomePageViewModel::class.java)
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        Log.d("TEST_GAME", "onCreate")

        initRequestGameDialog()

        val onlinePlayersAdapter = OnlinePlayersAdapter()
        initRecyclerView(onlinePlayersAdapter)

        compositeDisposable.add(
                RxView.clicks(online_game_button)
                        .flatMap { homePageViewModel.getOnlinePlayers() }
                        .doOnNext(onlinePlayersAdapter::updateList)
                        .concatMap { Observable.fromIterable(it) }
                        .flatMap(IOnlinePlayerEvent::getClick)
                        .map(IOnlinePlayerEvent::getPlayerId)
                        .subscribe(homePageViewModel::sendRequestOnlineGame)
        )

        compositeDisposable.add(
                RxView.clicks(computer_game_button)
                        .subscribe(Functions.actionConsumer(homePageViewModel::initComputerGame)))

        compositeDisposable.add(homePageViewModel.startGame(this)
                .subscribe { startActivityForResult(it, 50) })

        compositeDisposable.add(
                RxCompoundButton
                        .checkedChanges(change_availability)
                        .skipInitialValue()
                        .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .flatMapCompletable(homePageViewModel::setPlayerCanPlay)
                        .subscribe())

        compositeDisposable.add(
                homePageViewModel.changeAvailabilityGame(this)
                        .subscribe(change_availability::setChecked)
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileMoneyChanges()
                        .doOnNext {
                            Log.d("TEST_GAME", "HomePageActivity -> MONEY: $it")
                        }
                        .subscribe {
                            text_view_money_changes.text = it.toString()
                            Log.d("TEST_GAME", "HomePageActivity -> MONEY: $it")
                            CheckersApplication.create().showToast("YOUR MONEY IS: $it")
                        }
        )
    }

    private fun initRequestGameDialog() {
        RequestGameDialog(this, homePageViewModel::acceptOnlineGame, homePageViewModel::declineOnlineGame, compositeDisposable, homePageViewModel.getMsgState(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        compositeDisposable.add(homePageViewModel.setFinishGame(data).subscribe())
    }

    private fun initRecyclerView(onlinePlayersAdapter: OnlinePlayersAdapter) {
        recycler_view_players.adapter = onlinePlayersAdapter
        recycler_view_players.layoutManager = LinearLayoutManager(this)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
