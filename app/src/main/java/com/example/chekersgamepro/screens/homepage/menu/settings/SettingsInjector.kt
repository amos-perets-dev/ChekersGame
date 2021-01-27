package com.example.chekersgamepro.screens.homepage.menu.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.R
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.menu.settings.model.ILanguageItem
import com.example.chekersgamepro.screens.homepage.menu.settings.model.LanguageItem
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class SettingsInjector {
    private val onSelectedLanguage = BehaviorSubject.create<ILanguageItem>()

    fun createViewModelActivity(activity: FragmentActivity): SettingsViewModel {

        val repositoryManager = RepositoryManager.create()

        val languagesDataList = getLanguagesDataList()

        val settingsData = repositoryManager.getSettingsData()
                .doOnNext {
                    if (it.language != -1) {
                        onSelectedLanguage.onNext(languagesDataList[it.language])
                    } else {
                        setDefault()
                    }
                }
                .filter { it.language != -1 }


        val languagesAdapter = LanguagesAdapter(languagesDataList)
        val viewModel = SettingsViewModel(settingsData, repositoryManager, languagesAdapter, onSelectedLanguage.hide().distinctUntilChanged())

        val viewModelFactory = createViewModelFactory(viewModel)
        return ViewModelProviders.of(activity, viewModelFactory).get(SettingsViewModel::class.java)
    }

    private fun getLanguagesDataList(): ArrayList<ILanguageItem> {
        val arrayListOf = arrayListOf<ILanguageItem>(
                LanguageItem("Hebrew - עברית", R.drawable.ic_flag_israel, onSelectedLanguage),
                LanguageItem("English - English", R.drawable.ic_flag_united_states, onSelectedLanguage),
                LanguageItem("Russian - русский", R.drawable.ic_flag_russia, onSelectedLanguage)
        )
        arrayListOf.forEachIndexed { index, languageItem -> languageItem.setPosition(index) }
        return arrayListOf
    }

    private fun setDefault() {
        val displayLanguage = Locale.getDefault().displayLanguage
        if (displayLanguage == "עברית") {
            onSelectedLanguage.onNext(LanguageItem("Hebrew - עברית", R.drawable.ic_flag_israel, onSelectedLanguage))
        } else if (displayLanguage == "English") {
            onSelectedLanguage.onNext(LanguageItem("English - English", R.drawable.ic_flag_united_states, onSelectedLanguage))
        }
    }


    private fun createViewModelFactory(viewModel: Any): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return viewModel as T
            }
        }
    }

}