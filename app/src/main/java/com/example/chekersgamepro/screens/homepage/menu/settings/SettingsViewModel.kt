package com.example.chekersgamepro.screens.homepage.menu.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.menu.settings.model.ILanguageItem
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsViewModel(private val settingsData: Flowable<SettingsData>,
                        private val repositoryManager: RepositoryManager,
                        private val languagesAdapter: LanguagesAdapter,
                        private val onSelectedLanguage: Observable<ILanguageItem>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val isSaveData = PublishSubject.create<Boolean>()

    fun onClickAddSettings(selectedItemLanguagePosition: Int, checkedSound: Boolean, checkedChat: Boolean) {
        this.compositeDisposable.add(
                this.repositoryManager.addNewSettings(
                        SettingsData(language = selectedItemLanguagePosition, isSound = checkedSound, isChat = checkedChat)
                )
                        .doOnEvent {
                            Log.d("TEST_GAME", "SettingsViewModel onClickAddSettings doOnEvent")
                            isSaveData.onNext(true)
                        }
                        .subscribe()
        )
    }

    fun getSelectedItem() = onSelectedLanguage

    fun getLanguagesAdapter() = languagesAdapter

    fun isSave(): Observable<Boolean> =
            isSaveData.hide()
                    .subscribeOn(Schedulers.io())
                    .filter(Functions.equalsWith(true))
                    .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())


    fun getSettingData() = this.settingsData

    override fun onCleared() {
        Log.d("TEST_GAME", "SettingsViewModel onCleared")
        this.compositeDisposable.dispose()
        super.onCleared()
    }


}