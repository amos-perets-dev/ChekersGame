package com.example.chekersgamepro.models.player.game

import com.example.chekersgamepro.db.repository.manager.UserProfileManager
import java.io.Serializable

class PlayerGame(var playerNme: String = "",
                 levelPlayer: Int = 0,
                 var image: ByteArray = ByteArray(0)) : Serializable{

    var moneyGame : Int = UserProfileManager.FACTOR_MONEY * levelPlayer

}