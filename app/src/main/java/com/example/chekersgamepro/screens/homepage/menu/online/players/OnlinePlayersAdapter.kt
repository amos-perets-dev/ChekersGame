package com.example.chekersgamepro.screens.homepage.menu.online.players

import android.view.ViewGroup
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class OnlinePlayersAdapter(private val screenWidth: Int, private val screenHeight: Int) :
        CheckersRecyclerView.Companion.Adapter<IOnlinePlayerEvent>() {

    private var listPlayerEvents = ArrayList<IOnlinePlayerEvent>()

    private val clickOn = PublishSubject.create<CardPlayerState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckersRecyclerView.Companion.ViewHolder<IOnlinePlayerEvent> {
        return addViewHolder(OnlinePlayersViewHolder(parent, clickOn))
    }

    fun getClick(): Observable<CardPlayerState> =
            clickOn.hide()
                    .subscribeOn(Schedulers.io())

    override fun isNeedChangeItemSize() = true

    override fun getScreenHeight() = this.screenHeight

    override fun getScreenWidth() = this.screenWidth

    override fun getItem(position: Int): IOnlinePlayerEvent {
        return listPlayerEvents[position]
    }

    fun clear() {

    }
}