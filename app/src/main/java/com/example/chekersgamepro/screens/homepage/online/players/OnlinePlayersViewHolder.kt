package com.example.chekersgamepro.screens.homepage.online.players

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.online_player_item.view.*


class OnlinePlayersViewHolder(parent: ViewGroup
                              , private val clickOn: PublishSubject<CardPlayerState>) :
        CheckersRecyclerView.Companion.ViewHolder<IOnlinePlayerEvent>(parent, R.layout.online_player_item), View.OnClickListener {

    init {
        itemView.send_request_game_button.setOnClickListener(this)
    }

    override fun bindData(model: IOnlinePlayerEvent) {

        this.itemView.total_games_details.setTextTotalGames(model.getTotalWin(), model.getTotalLoss())

        val imageProfile = itemView.image_profile
        this.compositeDisposable.addAll(

                model.getPlayerName()
                        .subscribeOn(Schedulers.io())
                        .subscribe { itemView.name_player.text = it },

                model.getLevelPlayer()
                        .subscribeOn(Schedulers.io())
                        .subscribe { itemView.level_player.text = it },

                model.getImageProfile()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(imageProfile::setImageBitmap)
        )
    }

    override fun unBindData(model: IOnlinePlayerEvent) {
        this.compositeDisposable.clear()
        super.unBindData(model)
    }

    override fun onClick(view: View) {

        val imageProfile = itemView.image_profile

        val imagePair = Pair.create(imageProfile as View, ViewCompat.getTransitionName(imageProfile)!!)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation((itemView.context as Activity), imagePair)
        val onClickPlay = getDataModel()?.onClickPlay(options)

        this.clickOn.onNext(onClickPlay!!)
    }


}