package com.example.chekersgamepro.db.remote

import android.graphics.Point
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface IRemoteDb {

//    fun getUserProfile() : Single<IUserProfile>

    fun createUser(id: Long, userName: String): Single<IUserProfile>

    fun createPlayer(id: Long, userName: String): Single<IPlayer>

    fun isUserNameExistServer(userName: String): Single<Boolean>

    fun setIsCanPlay(isCanPlay : Boolean) : Completable

    fun getDataPlayerChanges() : Observable<IPlayer>

    fun getAllAvailableOnlinePlayersByLevel(playerName: String, level : Int): Observable<List<IOnlinePlayerEvent>>

    fun sendRequestOnlineGame(idRemotePlayer : Long) : Completable

    fun getRequestGameMsgText() : Observable<String>

    fun getRequestGameStatus() : Observable<RequestOnlineGameStatus>

    fun declineOnlineGame(): Completable

    fun acceptOnlineGame(): Completable

    fun finishRequestOnlineGame() : Completable

    fun getNowPlayer() : Observable<Int>

    fun setNowPlayer(): Completable

    fun notifyEndTurn(move: RemoteMove): Completable

    fun getRemoteMove() : Observable<RemoteMove>

    fun setFinishGameTechnicalLoss(): Completable

    fun isTechnicalWin(): Observable<Boolean>

    fun setMoney(money : Int) : Completable

    fun resetPlayer() : Completable

}