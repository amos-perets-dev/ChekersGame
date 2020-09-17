package com.example.chekersgamepro.screens.homepage.menu

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.menu.computer.ComputerGameViewModel
import io.reactivex.subjects.PublishSubject

class ComputerGameInjector {

    fun createViewModelActivity(activity: FragmentActivity): ComputerGameViewModel {

        val onClickButton = PublishSubject.create<Int>()

        val repositoryManager = RepositoryManager.create()
        val setIsCanPlay = repositoryManager.setIsCanPlay(true)

        val createPlayersGameIntent = repositoryManager.createPlayersGame(DataGame.Mode.COMPUTER_GAME_MODE)

        val viewModel = ComputerGameViewModel(setIsCanPlay, createPlayersGameIntent)

        val viewModelFactory = createViewModelFactory(viewModel)
        return ViewModelProviders.of(activity , viewModelFactory).get(ComputerGameViewModel::class.java)
    }

    private fun  createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return viewModel as T
            }
        }
    }

}