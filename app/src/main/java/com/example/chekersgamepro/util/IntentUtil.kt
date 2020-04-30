package com.example.chekersgamepro.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.game.PlayerGame
import com.example.chekersgamepro.screens.game.CheckersGameActivity
import io.reactivex.Single

class IntentUtil {

    companion object {

        private val checkersConfiguration = CheckersConfiguration.getInstance()

        fun createOpenGalleryIntent(): Intent {
//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT

                        return  Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)


//            return intent
        }

        fun createPlayersGameIntent(playerAsync: Single<IPlayer?>, gameMode: Int, imageUtil: CheckersImageUtil, context: Context) : Single<Intent>{
            return playerAsync
                    .map { player ->
                        val byteArrayPlayerTwoOwner = imageUtil.createByteArrayFromEncodeBase(getAvatarImageEncodeOwnerPlayer(gameMode, player))

                        val byteArrayGuestOrComputer = imageUtil.createByteArrayFromEncodeBase(getAvatarImageEncodeGuestOrComputerPlayer(gameMode, player))

                        val intent = Intent(context, CheckersGameActivity::class.java)

                        val guestOrComputerName = getPlayerNameGuestOrComputer(gameMode, player)
                        val playerOneGuestOrComputer = PlayerGame(guestOrComputerName, player.getLevelPlayer(), byteArrayGuestOrComputer)
                        val playerTwoOwner = PlayerGame(getPlayerNameTwoOwner(gameMode, player), player.getLevelPlayer(), byteArrayPlayerTwoOwner)


                        intent.putExtra("PLAYER_ONE", playerOneGuestOrComputer)
                        intent.putExtra("PLAYER_TWO", playerTwoOwner)
                        intent.putExtra("GAME_MODE", gameMode)

                        return@map intent

                    }
        }

        private fun getPlayerNameGuestOrComputer(gameMode: Int, player: IPlayer): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.isOwner()) player.getRemotePlayer() else player.getPlayerName()
                } else "COMPUTER"

        private fun getPlayerNameTwoOwner(gameMode: Int, player: IPlayer): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.isOwner()) player.getPlayerName() else player.getRemotePlayer()
                } else player.getPlayerName()

        private fun getAvatarImageEncodeGuestOrComputerPlayer(gameMode: Int, player: IPlayer): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.isOwner()) player.getAvatarRemotePlayer() else player.getEncodeImage()
                } else checkersConfiguration.getComputerIconEncode()

        private fun getAvatarImageEncodeOwnerPlayer(gameMode: Int, player: IPlayer): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.isOwner()) player.getEncodeImage() else player.getAvatarRemotePlayer()
                } else player.getEncodeImage()

    }

}