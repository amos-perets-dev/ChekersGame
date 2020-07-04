package com.example.chekersgamepro.screens.homepage

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersActivity
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarPickerFragment
import com.example.chekersgamepro.util.TouchListener
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

        compositeDisposable.add(
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


//        compositeDisposable.add(
//                homePageViewModel
//                        .startComputerGame(this)
//                        .subscribe { startActivityForResult(it, 55) }
//        )

        compositeDisposable.add(
                homePageViewModel
                        .startOnlineGame(this)
                        .subscribe { startActivityForResult(it, 50) }
        )

        image_profile_hp
                .setOnTouchListener(TouchListener(View.OnClickListener { onClickAvatar() }, 1.1f))

        compositeDisposable.add(
                homePageViewModel.openAvatarScreen(this)
                        .subscribe(Functions.actionConsumer(this::startAvatarPickerFragment))
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileName()
                        .subscribe(text_view_player_name::setText, Throwable::printStackTrace)
        )
        compositeDisposable.add(
                homePageViewModel
                        .getUserProfileLevelChanges()
                        .subscribe(text_view_level_changes::setText)
        )

        compositeDisposable.add(
                homePageViewModel.getImageProfileAsync()
                        .doOnNext { Log.d("TEST_GAME", "homePageViewModel.getImageProfileAsync()") }
                        .subscribe(image_profile_hp::setImageBitmap)
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileMoneyChanges()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { AnimationUtil.animatePulse(money_icon) }
                        .subscribe(text_view_money_changes::setText)
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileTotalLossChanges()
                        .subscribe(text_view_total_games::setTextTotalLoss)
        )

        compositeDisposable.add(
                homePageViewModel.getUserProfileTotalWinChanges()
                        .subscribe(text_view_total_games::setTextTotalWin)
        )

        compositeDisposable.add(AnimationUtil.animateViews(
                text_view_total_games,
                money_icon,
                text_view_player_name,
                text_view_money_changes,
                text_view_level_changes,
                image_profile_hp)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(homePageViewModel.isDefaultImage()
                        .doOnEvent { isDefaultImage, t2 ->
                            Log.d("TEST_GAME", "isDefaultImage: $isDefaultImage")
                            if (isDefaultImage!= null && isDefaultImage){
                                AnimationUtil.animatePulse(image_profile_hp, 0.8f)
                            }
                        })
                .subscribe())

    }

    private fun onClickAvatar() {
        this.homePageViewModel.onClickAvatar(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == FINISH_GAME) {
            compositeDisposable.add(homePageViewModel.finishGame(data!!).subscribe())
        }

    }

    override fun onDestroy() {
        Log.d("TEST_GAME", "HomePageActivity -> onDestroy")

        compositeDisposable.clear()
        super.onDestroy()
    }
}
