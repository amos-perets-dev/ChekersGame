package com.example.chekersgamepro.screens.homepage

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersActivity
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarPickerFragment
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogPlayersFragment
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import com.example.chekersgamepro.screens.homepage.online.players.OnlinePlayersFragment
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_page.*


open class HomePageActivity : CheckersActivity() {

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
        Log.d("TEST_GAME", "HomePageActivity onCreate")

        lifecycle.addObserver(homePageViewModel)
//        lifecycle.removeObserver(homePageViewModel)

        RxView.globalLayouts(image_profile_hp)
                .firstOrError()
                .subscribe { t1, t2 ->
                    val layoutParams = image_profile_hp.layoutParams
                    val ratio = resources.displayMetrics.widthPixels * 0.35
                    layoutParams.width = ratio.toInt()
                    layoutParams.height = ratio.toInt()
                    image_profile_hp.layoutParams = layoutParams
                    image_profile_hp.requestLayout()
                }

        compositeDisposable.add(
                homePageViewModel
                        .isOpenOnlinePlayers(this)
                        .subscribe {
                            startOnlinePlayersFragment()
                        }
        )

        compositeDisposable.add(
                homePageViewModel.isDefaultImage()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            AnimationUtil.animatePulse(image_profile_hp)
                        }
        )

        compositeDisposable.add(
                homePageViewModel
                        .getMsgState(this)
                        .doOnNext {
                            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else { //deprecated in API 26
                                v.vibrate(1000)
                            }
                        }
                        .doOnNext(this::startActivity)
                        .subscribe { overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out) }
        )

        compositeDisposable.add(
                RxView.clicks(online_game_button)
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            homePageViewModel.clickOnOnlineGame()
                        }
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

        compositeDisposable.add(
                RxView.clicks(computer_game_button)
                        .subscribe(Functions.actionConsumer(homePageViewModel::clickOnComputerGame))
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


    private fun startOnlinePlayersFragment() {
        val onlinePlayersFragment: Fragment? = getOnlinePlayersFragment()
        if (onlinePlayersFragment != null) {
            (onlinePlayersFragment as DialogFragment).show(supportFragmentManager, "online_players")
        }
    }

    private fun startDialogPlayersFragment(dialogStateCreator: DialogStateCreator) {
        val dialogPlayersFragment: Fragment? = getDialogPlayersFragment(dialogStateCreator)
        if (dialogPlayersFragment != null) {
            (dialogPlayersFragment as DialogFragment).show(supportFragmentManager, "dialog_players")
        }
    }

    private fun getDialogPlayersFragment(dialogStateCreator: DialogStateCreator): Fragment? {
        return DialogPlayersFragment.newInstance(image_profile_hp, dialogStateCreator)
    }

    private fun getAvatarPickerFragment(): Fragment? {
        return AvatarPickerFragment.newInstance(image_profile_hp)
    }

    private fun getOnlinePlayersFragment(): Fragment? {
        return OnlinePlayersFragment.newInstance(image_profile_hp)
    }


//    private fun closePlayersList() {
//        players_list_container.animate().withLayer().translationY((players_list_container.measuredHeight).toFloat()).start()
//    }

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
//        RequestGameDialog(this
//                , homePageViewModel::acceptOnlineGame
//                , homePageViewModel::declineOnlineGame
//                , compositeDisposable
//                , homePageViewModel.getMsgState(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == FINISH_GAME) {
            compositeDisposable.add(homePageViewModel.finishGame(data!!).subscribe())
        }

    }

//    private fun initRecyclerView(onlinePlayersAdapter: OnlinePlayersAdapter) {
//        recycler_view_players.adapter = onlinePlayersAdapter
//        val linearLayoutManager = LinearLayoutManager(this)
//        linearLayoutManager.isAutoMeasureEnabled = false
//
//        recycler_view_players.layoutManager = linearLayoutManager
//    }

    override fun onPause() {
        Log.d("TEST_GAME", "HomePageActivity -> onPause")


        super.onPause()
    }


    override fun onStop() {
//        Log.d("TEST_GAME", "HomePageActivity -> onStop")
//
//        val fragment = (supportFragmentManager.findFragmentByTag("avatar_picker") as AvatarPickerFragment?)
//        if (fragment != null && fragment.isVisible) {
//            fragment.dismissAllowingStateLoss()
//            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
//        }
        super.onStop()
    }


    override fun onDestroy() {
        Log.d("TEST_GAME", "HomePageActivity -> onDestroy")

        compositeDisposable.clear()
        super.onDestroy()
    }
}
