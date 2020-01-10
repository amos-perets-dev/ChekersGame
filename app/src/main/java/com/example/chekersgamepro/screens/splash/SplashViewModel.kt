package com.example.chekersgamepro.screens.splash

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.registration.RegistrationActivity
import com.example.chekersgamepro.util.CheckersApplication

class SplashViewModel : ViewModel() {

    private val repositoryManager = RepositoryManager.create()

    private val context = CheckersApplication.create()


    fun getNextClass(): Intent = if (repositoryManager.isRegistered()) {
        Intent(context, HomePageActivity::class.java)
    } else {
        Intent(context, RegistrationActivity::class.java)
    }


    fun setRunFirstTime() {
        repositoryManager.setRunFirstTime()
    }


}