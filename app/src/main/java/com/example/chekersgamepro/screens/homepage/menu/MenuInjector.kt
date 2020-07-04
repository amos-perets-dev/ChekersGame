package com.example.chekersgamepro.screens.homepage.menu

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.db.repository.RepositoryManager
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class MenuInjector {

    fun createViewModelActivity(activity: FragmentActivity): MenuViewModel {

        val onClickButton = PublishSubject.create<Int>()

        val menuButtonsData = MenuButtonsData(CheckersApplication.create().applicationContext.resources, onClickButton)

        val repositoryManager = RepositoryManager.create()
        val setIsCanPlay = repositoryManager.setIsCanPlay(true)

        val createPlayersGameIntent = repositoryManager.createPlayersGame(DataGame.Mode.COMPUTER_GAME_MODE)

        val click = onClickButton.hide().subscribeOn(Schedulers.io())

        val viewModel = MenuViewModel(menuButtonsData.buttonsList, click, setIsCanPlay, createPlayersGameIntent)

        val viewModelFactory = createViewModelFactory(viewModel)
        return ViewModelProviders.of(activity , viewModelFactory).get(MenuViewModel::class.java)
    }

    private fun  createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return viewModel as T
            }
        }
    }

}