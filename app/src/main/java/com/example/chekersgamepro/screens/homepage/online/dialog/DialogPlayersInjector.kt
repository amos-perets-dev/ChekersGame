package com.example.chekersgamepro.screens.homepage.online.dialog

import android.graphics.Bitmap
import androidx.core.util.Pair
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import io.reactivex.Observable

class DialogPlayersInjector {


    companion object {
        fun createViewModelActivity(activity: FragmentActivity): DialogPlayersViewModel {
            val repositoryManager = RepositoryManager.create()

            val dialogStateCreator = repositoryManager.getDialogState()

            val player = dialogStateCreator
                    .map { it.remotePlayerMsg }

            val viewModel = createViewModelObject(
                    dialogStateCreator
                    , player.flatMap { it.getImageProfile() }
                    , player.flatMap { it.getPlayerName() }
                    , player.map {Pair.create(it.getTotalWin(), it.getTotalLoss())}
                    , player.flatMap { it.getLevelPlayer() }
                    , repositoryManager.getRequestGameStatus())

            val viewModelFactory = createViewModelFactory(viewModel)
            return ViewModelProviders.of(activity , viewModelFactory).get(DialogPlayersViewModel::class.java)
        }

        private fun createViewModelObject(
                dialogStateCreator: Observable<DialogStateCreator>,
                remotePlayerAvatar: Observable<Bitmap>,
                remotePlayerName: Observable<String>,
                remotePlayerTotalGames: Observable<Pair<String, String>>,
                remotePlayerLevel: Observable<String>,
                requestGameStatus: Observable<RequestOnlineGameStatus>): DialogPlayersViewModel{

            val msgByState = dialogStateCreator.map { it.msgByState }
            val dialogState = dialogStateCreator.map { it.dialogState }

            return DialogPlayersViewModel(
                    msgByState
                    , dialogState
                    , remotePlayerAvatar
                    , remotePlayerName
                    , remotePlayerTotalGames
                    , remotePlayerLevel
                    , CheckersApplication.create().applicationContext
                    , requestGameStatus)

        }

        private fun  createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return viewModel as T
                }
            }
        }

    }
}