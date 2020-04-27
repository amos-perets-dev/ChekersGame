package com.example.chekersgamepro.screens.splash

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_splash.*


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

        compositeDisposable.add( splashViewModel.setImageDefaultPreUpdate()
                .subscribe { t1, t2 ->
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

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
