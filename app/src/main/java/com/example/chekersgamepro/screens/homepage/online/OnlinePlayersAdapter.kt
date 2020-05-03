package com.example.chekersgamepro.screens.homepage.online

import android.view.ViewGroup
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent

class OnlinePlayersAdapter() : CheckersRecyclerView.Companion.Adapter<IOnlinePlayerEvent>() {

    private var listPlayerEvents = ArrayList<IOnlinePlayerEvent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckersRecyclerView.Companion.ViewHolder<IOnlinePlayerEvent> {

        return OnlinePlayersViewHolder(parent)
    }

    override fun getItem(position: Int): IOnlinePlayerEvent {
        return listPlayerEvents[position]
    }
}