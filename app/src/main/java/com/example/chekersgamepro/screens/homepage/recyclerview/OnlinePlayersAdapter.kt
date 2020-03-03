package com.example.chekersgamepro.screens.homepage.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent

class OnlinePlayersAdapter : RecyclerView.Adapter<OnlinePlayersViewHolder>() {

    private var listPlayerEvents = ArrayList<IOnlinePlayerEvent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlinePlayersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.online_player_item, parent, false)

        return OnlinePlayersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listPlayerEvents.size
    }

    override fun onBindViewHolder(holder: OnlinePlayersViewHolder, position: Int) {
        holder.bindData(listPlayerEvents[position])
    }

    fun updateList(listPlayerEvents : List<IOnlinePlayerEvent>){
        this.listPlayerEvents = ArrayList(listPlayerEvents)
        notifyDataSetChanged()
    }
}