package com.example.chekersgamepro.models.player.card

import androidx.core.app.ActivityOptionsCompat

class CardPlayerState(val playerId: Long, var cardStateEvent: PlayerCardStateEvent, var options: ActivityOptionsCompat?) {

    constructor(playerCardStateEvent: PlayerCardStateEvent) : this(-1L, playerCardStateEvent, null)

    override fun toString(): String {
        return "PlayerState(playerId=$playerId, cardState=$cardStateEvent)"
    }


}