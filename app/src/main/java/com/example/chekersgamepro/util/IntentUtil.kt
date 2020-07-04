package com.example.chekersgamepro.util

import android.content.Context
import android.content.Intent
import com.example.chekersgamepro.BuildConfig
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.models.player.data.PlayerData
import com.example.chekersgamepro.models.player.game.PlayerGame
import com.example.chekersgamepro.screens.game.CheckersGameActivity
import io.reactivex.Observable
import io.reactivex.Single

class IntentUtil {

    companion object {

        private val checkersConfiguration = CheckersConfiguration.getInstance()

        fun createOpenGalleryIntent(): Intent  =   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        fun createPlayersGameIntent(playerAsync: Observable<PlayerData>, gameMode: Int, imageUtil: CheckersImageUtil, context: Context) : Single<Intent>{
            return playerAsync
                    .map { player ->
                        val byteArrayPlayerTwoOwner = imageUtil.createByteArrayFromEncodeBase(getAvatarImageEncodeOwnerPlayer(gameMode, player))

                        val byteArrayGuestOrComputer = imageUtil.createByteArrayFromEncodeBase(getAvatarImageEncodeGuestOrComputerPlayer(gameMode, player))

                        val intent = Intent(context, CheckersGameActivity::class.java)

                        val guestOrComputerName = getPlayerNameGuestOrComputer(gameMode, player)
                        val playerOneGuestOrComputer = PlayerGame(guestOrComputerName, player.userLevel, byteArrayGuestOrComputer)
                        val playerTwoOwner = PlayerGame(getPlayerNameTwoOwner(gameMode, player), player.userLevel, byteArrayPlayerTwoOwner)


                        intent.putExtra("PLAYER_ONE", playerOneGuestOrComputer)
                        intent.putExtra("PLAYER_TWO", playerTwoOwner)
                        intent.putExtra("GAME_MODE", gameMode)

                        return@map intent

                    }
                    .firstOrError()
        }

        fun createAppShareIntent(): Intent  {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            return  shareIntent
        }

        private fun getPlayerNameGuestOrComputer(gameMode: Int, player: PlayerData): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.owner) player.remotePlayerActive.remotePlayerName else player.playerName
                } else "COMPUTER"

        private fun getPlayerNameTwoOwner(gameMode: Int, player: PlayerData): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.owner) player.playerName else player.remotePlayerActive.remotePlayerName
                } else player.playerName

        private fun getAvatarImageEncodeGuestOrComputerPlayer(gameMode: Int, player: PlayerData): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.owner) player.remotePlayerActive.remotePlayerAvatar else player.avatarEncodeImage
                } else checkersConfiguration.getComputerIconEncode()

        private fun getAvatarImageEncodeOwnerPlayer(gameMode: Int, player: PlayerData): String =
                if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                    if (player.owner) player.avatarEncodeImage else player.remotePlayerActive.remotePlayerAvatar
                } else player.avatarEncodeImage
    }

}