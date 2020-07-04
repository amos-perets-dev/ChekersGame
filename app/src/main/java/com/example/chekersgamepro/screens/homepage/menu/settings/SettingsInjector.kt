package com.example.chekersgamepro.screens.homepage.menu.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.db.repository.RepositoryManager

class SettingsInjector {

    fun createViewModelActivity(activity: FragmentActivity): SettingsViewModel {

        val repositoryManager = RepositoryManager.create()

        val settingsData = repositoryManager.getSettingsData()

        val viewModel = SettingsViewModel(settingsData, repositoryManager)

        val viewModelFactory = createViewModelFactory(viewModel)
        return ViewModelProviders.of(activity , viewModelFactory).get(SettingsViewModel::class.java)
    }

    private fun  createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return viewModel as T
            }
        }
    }

}