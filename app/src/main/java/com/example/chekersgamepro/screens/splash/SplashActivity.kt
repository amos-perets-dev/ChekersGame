package com.example.chekersgamepro.screens.splash

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R
import com.example.chekersgamepro.util.animation.AnimationUtil
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.concurrent.TimeUnit


class SplashActivity : AppCompatActivity() {

    private val splashViewModel by lazy {
        ViewModelProviders.of(this@SplashActivity).get(SplashViewModel::class.java)
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = splashViewModel.getNextClass()

        splashViewModel.setRunFirstTime()

        compositeDisposable.add(splashViewModel.setImageDefaultPreUpdate()
                .subscribe { t1, t2 ->

                }
        )

        compositeDisposable.add(
                AnimationUtil.scaleWithRotation(logo_checkers, 1500)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent { animateGameText() }
                .delay(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe{
                    startActivity(intent)
                    finish()
                }
        )


//        splashViewModel.setRunFirstTime()
//        var index = 0
//        splashViewModel.getImage(this)
//                .doOnNext {
//                    image_test.setImageBitmap(it)
//                    index++
//                    Log.d("TEST_GAME", "doOnNext index: $index")
//                }
//                .subscribe{
//                    Log.d("TEST_GAME", "subscribe index: $index")
//                    if (index == 4){
//                        startActivity(intent)
//                        finish()
//                    }
//
//                }

    }

    private fun animateGameText() {
        Log.d("TEST_GAME", "SplashActivity -> animateGameText")

        logo_game.setCharacterDelay(150)
        logo_game.animateText("GAME")
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
