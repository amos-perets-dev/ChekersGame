package com.example.chekersgamepro.screens.splash

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R


class SplashActivity : AppCompatActivity() {

    private val splashViewModel by lazy {
        ViewModelProviders.of(this@SplashActivity).get(SplashViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var intent = splashViewModel.getNextClass()

        splashViewModel.setRunFirstTime()

        startActivity(intent)
        finish()

    }

}
