package com.example.chekersgamepro.db.repository.manager

import android.util.Log
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.models.player.data.PlayerData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class PlayerManager(private val realmManager: RealmManager, private val remoteDb: IRemoteDb) {

    private val playerAsync = BehaviorSubject.create<PlayerData>()

    private var player : PlayerData? = null

    init {
        remoteDb.createPlayer()
                .filter { it.isPresent }
                .map { it.get() }
                .doOnEvent { player, t2 ->  this.player = player}
                .subscribe()
    }

    fun startGetDataPlayerChanges(){
        remoteDb.getDataPlayerChanges()
                .doOnNext {
                    this.player = it
                    Log.d("TEST_GAME", "PlayerManager startGetDataPlayerChanges doOnNext player: ${this.player.toString()}")
                }
                .doOnNext { playerAsync.onNext(it) }
                .subscribe ()
    }

    fun createPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable =
            remoteDb.createPlayer(id, userName, encodeImageDefaultPreUpdate)
                    .filter{it.isPresent}
                    .map { it.get() }
                    .flatMapCompletable{
                        this.player = it
                        Completable.complete()
                    }

    fun createTopPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable =
        remoteDb.createTopPlayer(id, userName, encodeImageDefaultPreUpdate)

    fun isOwnerPlayerAsync(): Observable<Boolean> = getPlayerAsync().map(PlayerData::owner).distinctUntilChanged()

    fun getPlayerAsync() : Observable<PlayerData> = playerAsync.hide()

    fun getNowPlayAsync(): Observable<Int> = getPlayerAsync().map(PlayerData::nowPlay).distinctUntilChanged()

    fun sendRequestOnlineGame(remotePlayerId: Long): Completable =  remoteDb.sendRequestOnlineGame(remotePlayerId)

    fun declineOnlineGame(): Completable =  remoteDb.declineOnlineGame()

    fun acceptOnlineGame(): Completable =  remoteDb.acceptOnlineGame()

    fun setIsCanPlay(isCanPlay: Boolean): Completable =  remoteDb.setIsCanPlay(isCanPlay)!!

    fun getRemoteMove(): Observable<RemoteMove> = remoteDb.getRemoteMove()

    fun resetPlayer(): Completable = remoteDb.resetPlayer()


}