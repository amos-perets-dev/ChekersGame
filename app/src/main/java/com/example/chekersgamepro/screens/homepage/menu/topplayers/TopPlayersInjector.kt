package com.example.chekersgamepro.screens.homepage.menu.topplayers

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.db.repository.RepositoryManager

class TopPlayersInjector {

    fun createViewModelActivity(activity: FragmentActivity): TopPlayersViewModel {

        val repositoryManager = RepositoryManager.create()


        val viewModel = TopPlayersViewModel(repositoryManager.getTopPlayersList())


        val viewModelFactory = createViewModelFactory(viewModel)
        return ViewModelProviders.of(activity , viewModelFactory).get(TopPlayersViewModel::class.java)
    }

    private fun  createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return viewModel as T
            }
        }
    }

}