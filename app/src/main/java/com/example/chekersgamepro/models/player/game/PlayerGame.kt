package com.example.chekersgamepro.models.player.game

import com.example.chekersgamepro.db.repository.manager.UserProfileManager
import java.io.Serializable

class PlayerGame(name: String = ""
                 , levelPlayer: Int = 0
                 , byteArray: ByteArray = ByteArray(0)) : Serializable{

    var playerNme : String = name
    var moneyGame : Int = UserProfileManager.FACTOR_MONEY * levelPlayer
    var image : ByteArray = byteArray

}