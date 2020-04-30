package com.example.chekersgamepro.screens.homepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarPickerFragment
import com.example.chekersgamepro.screens.homepage.dialog.RequestGameDialog
import com.example.chekersgamepro.screens.homepage.online.OnlinePlayersAdapter
import com.example.chekersgamepro.util.SwipeUtil
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_page.*


open class HomePageActivity : AppCompatActivity() {

    private val LOAD_IMG_REQUEST = 100
    private val CAMERA_REQUEST = 200

    companion object {
        @JvmField
        var FINISH_GAME = 200
    }

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
                homePageViewModel.isDefaultImage()
                        .subscribe {
                            AnimationUtil.animatePulse(image_profile_hp)
                        }
        )

        compositeDisposable.add(
                homePageViewModel.getOnlinePlayers()
                        .subscribeOn(Schedulers.io())
                        .doOnNext(onlinePlayersAdapter::updateList)
                        .concatMap { Observable.fromIterable(it) }
                        .flatMap(IOnlinePlayerEvent::getClick)
                        .map(IOnlinePlayerEvent::getPlayerId)
                        .subscribe(homePageViewModel::sendRequestOnlineGame)
        )

        compositeDisposable.add(
                RxView.clicks(online_game_button)
                        .doOnNext { players_list_container.animate().withLayer().translationY(0F).start() }
                        .subscribe()
        )

        compositeDisposable.add(
                homePageViewModel
                        .startComputerGame(this)
                        .subscribe { startActivityForResult(it, 55) }
        )

        compositeDisposable.add(
                homePageViewModel
                        .startOnlineGame(this)
                        .subscribe { startActivityForResult(it, 50) }
        )

        close_player_list.setOnTouchListener(SwipeUtil(this, this::closePlayersList))

        compositeDisposable.add(
                RxView.clicks(computer_game_button)
                        .subscribe(Functions.actionConsumer(homePageViewModel::initComputerGame))
        )

        compositeDisposable.add(
                homePageViewModel.getPlayerName()
                        .subscribe(text_view_player_name::setText)
        )

        // For the first time
        compositeDisposable.add(
                homePageViewModel.getUserProfileMoney()
                        .subscribe { money, it2 ->
                            text_view_money_changes.text = money.toString()
                        }
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileMoneyChanges()
                        .doOnNext { AnimationUtil.animatePulse(money_icon) }
                        .map { it.toString() }
                        .doOnNext(text_view_money_changes::setText)
                        .subscribe()
        )

        // For the first time
        compositeDisposable.add(
                homePageViewModel.getImageProfile()
                        .subscribe { imageProfile, t2 -> image_profile_hp.setImageBitmap(imageProfile) }
        )

        compositeDisposable.add(
                homePageViewModel.getImageProfileAsync()
                        .subscribe(image_profile_hp::setImageBitmap)
        )

        compositeDisposable.add(
                RxView.clicks(image_profile_hp)
                        .subscribe { homePageViewModel.clickOnAvatar(this) }
//                        .subscribe(Functions.actionConsumer(this::startAvatarPickerFragment))
        )

        compositeDisposable.add(
                homePageViewModel.openAvatarScreen(this)
                        .subscribe(Functions.actionConsumer(this::startAvatarPickerFragment))
        )

        compositeDisposable.add(
                homePageViewModel
                        .getUserProfileLevelChanges()
                        .subscribe(text_view_level_changes::setText)
        )

        compositeDisposable.add(AnimationUtil.animateViews(
                money_icon, text_view_player_name, text_view_money_changes, text_view_level_changes, image_profile_hp
                , computer_game_button, online_game_button)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe())

    }

    private fun startAvatarPickerFragment() {
        val avatarPickerFragment: Fragment? = getAvatarPickerFragment()
        if (avatarPickerFragment != null) {
            (avatarPickerFragment as DialogFragment).show(supportFragmentManager, "avatar_picker")
        }
    }

    private fun getAvatarPickerFragment(): Fragment? {
        return AvatarPickerFragment.newInstance(image_profile_hp)
    }

    private fun closePlayersList() {
        players_list_container.animate().withLayer().translationY((players_list_container.measuredHeight).toFloat()).start()
    }

    override fun onResume() {
        super.onResume()
//        Log.d("TEST_GAME", "HomePageActivity -> onResume -> measuredHeight: ${recycler_view_players.measuredHeight.toFloat()}")
        Log.d("TEST_GAME", "HomePageActivity -> onResume")

    }

    override fun onStart() {
        super.onStart()
        Log.d("TEST_GAME", "HomePageActivity -> onStart")

//        Log.d("TEST_GAME", "HomePageActivity -> onStart -> measuredHeight: ${recycler_view_players.measuredHeight.toFloat()}")

    }


    private fun initRequestGameDialog() {
        RequestGameDialog(this
                , homePageViewModel::acceptOnlineGame
                , homePageViewModel::declineOnlineGame
                , compositeDisposable
                , homePageViewModel.getMsgState(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == FINISH_GAME) {
            compositeDisposable.add(homePageViewModel.setFinishGame(data!!).subscribe())
        }

    }

    private fun initRecyclerView(onlinePlayersAdapter: OnlinePlayersAdapter) {
        recycler_view_players.adapter = onlinePlayersAdapter
        recycler_view_players.layoutManager = LinearLayoutManager(this)
    }

    override fun onPause() {
        Log.d("TEST_GAME", "HomePageActivity -> onPause")


        super.onPause()
    }


    override fun onStop() {
        Log.d("TEST_GAME", "HomePageActivity -> onStop")

        val fragment = (supportFragmentManager.findFragmentByTag("avatar_picker") as AvatarPickerFragment?)
        if (fragment != null && fragment.isVisible) {
            fragment.dismissAllowingStateLoss()
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        super.onStop()
    }


    override fun onDestroy() {
        Log.d("TEST_GAME", "HomePageActivity -> onDestroy")

        compositeDisposable.clear()
        super.onDestroy()
    }
}
