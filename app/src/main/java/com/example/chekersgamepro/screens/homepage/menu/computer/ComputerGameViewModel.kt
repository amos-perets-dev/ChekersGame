package com.example.chekersgamepro.screens.homepage.menu.computer

import android.content.Intent
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.data.data_game.DataGame
import io.reactivex.Completable
import io.reactivex.Single

class ComputerGameViewModel(private val isCanPlay: Completable,
                            private val createPlayersGameIntent: Single<Intent>) : ViewModel() {

    fun onClickComputerGame(level: CharSequence): Single<Intent> {
        if (level.isDigitsOnly()){
            DataGame.getInstance().difficultLevel = level.toString().toInt()
        }
        return this.isCanPlay
                .andThen(this.createPlayersGameIntent)
    }

    fun getTextLevel(progress: Int) : String{
        return (((progress / 2.6).toInt()) + 1).toString()
    }

}