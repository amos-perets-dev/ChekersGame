package com.example.chekersgamepro.screens.homepage.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import kotlinx.android.synthetic.main.online_player_item.view.*

class OnlinePlayersViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener  {

    private lateinit var playerEvent : IOnlinePlayerEvent

    init {
        itemView.name_player.setOnClickListener(this)
        itemView.level_player.setOnClickListener(this)
    }

    fun bindData(playerEvent : IOnlinePlayerEvent){
        this.playerEvent = playerEvent
        itemView.name_player.text = playerEvent.getPlayerName()
        itemView.level_player.text = playerEvent.getLevelPlayer().toString()

    }

    override fun onClick(view : View) {
        playerEvent.onClickItem(playerEvent)
    }


}