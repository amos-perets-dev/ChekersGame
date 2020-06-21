package com.example.chekersgamepro.screens.homepage.topplayers

import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class TopPlayersViewModel(private val topPlayersList: Observable<List<ITopPlayer>>) : ViewModel() {

    fun getTopPlayersList() = this.topPlayersList

}