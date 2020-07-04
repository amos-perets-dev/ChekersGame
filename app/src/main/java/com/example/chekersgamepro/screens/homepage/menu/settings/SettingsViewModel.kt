package com.example.chekersgamepro.screens.homepage.menu.settings

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.db.repository.RepositoryManager
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsViewModel(private val settingsData: Flowable<SettingsData>,
                        private val repositoryManager: RepositoryManager) : ViewModel() {

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


    fun isSave (): Observable<Boolean> =
         isSaveData.hide()
                .filter(Functions.equalsWith(true))
                .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())


    fun getSettingData() = this.settingsData

    override fun onCleared() {
        Log.d("TEST_GAME", "SettingsViewModel onCleared")
        this.compositeDisposable.dispose()
        super.onCleared()
    }



}