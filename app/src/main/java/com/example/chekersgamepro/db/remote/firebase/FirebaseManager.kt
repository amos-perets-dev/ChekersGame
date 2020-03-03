package com.example.chekersgamepro.db.remote.firebase

import android.util.Log
import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.PlayerImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.util.NetworkUtil
import com.google.common.base.Optional
import com.google.firebase.database.*
import io.reactivex.*


class FirebaseManager {

    private val databaseUsers = FirebaseDatabase.getInstance().getReference("Users")
    private val databasePlayers = FirebaseDatabase.getInstance().getReference("CenterPlayers")

    private val networkUtil = NetworkUtil()


    init {

    }

    public fun addNewUser(user: IUserProfile): Single<Boolean> {
        if (!networkUtil.isAvailableNetwork()) {
            return Single.error(Throwable())
        }

        return Single.create {
            databaseUsers
                    .child(user.getUserName())
                    .setValue(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onSuccess(true)
                            //Do what you need to do
                        } else {
                            it.onError(Throwable("ERROR ADD NEW USER"))
                        }
                    }
        }
    }

    public fun addNewPlayer(player: IPlayer): Single<Boolean> {

        if (!networkUtil.isAvailableNetwork()) {
            return Single.error(Throwable("NO INTERNET CONNECTION"))
        }

        return Single.create {
            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(player.getPlayerName())
                    .setValue(player)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onSuccess(true)
                            //Do what you need to do
                        } else {
                            it.onError(Throwable("ERROR ADD NEW PLAYER"))
                        }
                    }
        }

    }

    public fun isUserNameExist(userName: String): Single<Boolean> {
        val applesQuery = databaseUsers.child(userName)

        return RxFirebaseDatabase.data(applesQuery)
                .map(DataSnapshot::getChildren)
                .map { it.toSet() }
                .map { it.size }
                .flatMap { Single.just(it != 0) }
    }

    fun setIsCanPlay(playerName: String, level: String, isCanPlay: Boolean): Completable {

        return Completable.create {
            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("canPlay")
                    .setValue(isCanPlay)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onComplete()
                            //Do what you need to do
                        } else {
                            it.onError(Throwable("ERROR SET CAN PLAY"))
                        }
                    }
        }
    }

    public fun getRequestStatusChanges(playerName: String, level: String): Observable<RequestOnlineGameStatus> {
        val query = databasePlayers
                .child(level)
                .child(playerName)
                .child("requestOnlineGameStatus")

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(RequestOnlineGameStatus::class.java) }

    }

    public fun getPlayerChanges(playerName: String, level: String): Observable<IPlayer> {
        val query = databasePlayers.child(level).child(playerName)

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(PlayerImpl::class.java) }
                .cast(IPlayer::class.java)

    }

    fun deletePlayer(player: PlayerImpl, isNeedUpdateLevel: Boolean): Single<Boolean> {

        // if don't need to update the level player
        // so dont need to delete the player from the current level
        if (!isNeedUpdateLevel) return Single.just(true)

        var databaseReference = databasePlayers.child((player.getLevelPlayer() - 1).toString()).child(player.getPlayerName())
        return Single.create { emitter ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.value != null || dataSnapshot.ref != null) {

                        val removeValue = dataSnapshot.ref.removeValue()

                        removeValue.addOnSuccessListener {
                            emitter.onSuccess(true)
                        }

                        removeValue.addOnFailureListener {
                            emitter.onError(Throwable(it))
                        }

                    } else {
                        emitter.onError(Throwable("dataSnapshot.value == null"))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    emitter.onError(Throwable(databaseError.message))
                }

            })
        }
    }

    fun setDataPlayer(player: IPlayer, isNeedUpdateLevel: Boolean): Single<Boolean> {
        return deletePlayer(player as PlayerImpl, isNeedUpdateLevel)
                .doOnEvent { isFinished, t2 ->
                    if (isFinished) {
                        databasePlayers.child(player.getLevelPlayer().toString()).child(player.getPlayerName()).setValue(player)
                    }
                }
    }

    fun getAllPlayersByLevel(levelPlayer: Int): Observable<MutableIterable<DataSnapshot>> {

        val databaseReference = databasePlayers.child(levelPlayer.toString())

        return RxFirebaseDatabase.dataChanges(databaseReference)
                .map { it.children }
    }

    fun sendRequestOnlineGame(remotePlayer: IPlayer, player: IPlayer): Completable {
        return Completable.create { emitter ->

            databasePlayers
                    .child(remotePlayer.getLevelPlayer().toString())
                    .child(remotePlayer.getPlayerName())
                    .setValue(remotePlayer)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            emitter.onError(Throwable("ERROR REQUEST GAME(remotePlayer)"))
                        }
                    }

            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(player.getPlayerName())
                    .setValue(player)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable("ERROR REQUEST GAME(player)"))
                        }
                    }
        }

    }

    fun acceptOnlineGame(remotePlayer: String, player: String, level: String): Completable {

        return Completable.create {
            databasePlayers
                    .child(level)
                    .child(remotePlayer)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.ACCEPT_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Do what you need to do
                        } else {
                            it.onError(Throwable())
                        }
                    }

            databasePlayers
                    .child(level)
                    .child(player)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.ACCEPT_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onComplete()
                            //Do what you need to do
                        } else {
                            it.onError(Throwable())
                        }
                    }
        }


    }

    fun declineOnlineGame(remotePlayer: String, player: IPlayer): Completable {

        return Completable.create {

            // The remote player is the owner
            // that need to notify on the decline game
            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(remotePlayer)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.DECLINE_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            it.onError(Throwable("ERROR DECLINE GAME"))
                        }
                    }

            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(player.getPlayerName())
                    .setValue(player)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onComplete()
                            //Do what you need to do
                        } else {
                            it.onError(Throwable("ERROR DECLINE GAME"))
                        }
                    }

        }

    }

    fun setNowPlayer(isLocalPlayerOwner: Boolean, playerName: String, remotePlayer: String, level: String): Completable {

        return Completable.create {
            databasePlayers
                    .child(level)
                    .child(remotePlayer)
                    .child("nowPlay")
                    .setValue(if (isLocalPlayerOwner) PlayersCode.PLAYER_TWO.ordinal else PlayersCode.PLAYER_ONE.ordinal)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            it.onError(Throwable())
                        }
                    }

            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("nowPlay")
                    .setValue(if (isLocalPlayerOwner) PlayersCode.PLAYER_TWO.ordinal else PlayersCode.PLAYER_ONE.ordinal)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onComplete()
                        } else {
                            it.onError(Throwable())
                        }
                    }
        }

    }

    fun getNowPlayer(playerName: String, level: String): Observable<Int> {

        val query = databasePlayers
                .child(level)
                .child(playerName)
                .child("nowPlay")

        return RxFirebaseDatabase.dataChanges(query)
                .map { (it.value as Long).toInt() }
    }

    fun getRemoteMove(playerName: String, level: String): Observable<RemoteMove> {

        val query = databasePlayers
                .child(level)
                .child(playerName)
                .child("remoteMove")

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(RemoteMove::class.java) }
    }

    fun notifyMove(remoteMove: RemoteMove, playerName: String, level: String): Completable {
        return Completable.create {
            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("remoteMove")
                    .setValue(remoteMove)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.onComplete()
                        } else {
                            it.onError(Throwable("ERROR DECLINE GAME"))
                        }
                    }
        }
    }

    fun pingFinishGameTechnicalLoss(playerName: String, level: String/*, isPingTechnicalLoss : Boolean*/): Completable {
        return Completable.create { emitter ->
            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("technicalLoss")
                    .setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()

                        } else {
                            emitter.onError(Throwable("PING FINISH GAME TECHNICAL LOSS ERROR"))
                        }
                    }
        }
    }

    fun isTechnicalWin(remotePlayer: String, level: String): Observable<Boolean> {

        val query = databasePlayers
                .child(level)
                .child(remotePlayer)
                .child("technicalLoss")

        return RxFirebaseDatabase.dataChanges(query)
                .map { Optional.of(it) }
                .filter { it.isPresent }
                .map { it.get() }
                .map { it.value is Boolean }
    }

    fun setMoney(userName: String, money: Int): Completable {
        return Completable.create { emitter ->
            databaseUsers
                    .child(userName)
                    .child("money")
                    .setValue(money)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TEST_GAME", "FirebaseManager -> setMoney: ${task.isSuccessful}")

                            emitter.onComplete()
                        } else {
                            emitter.onError(Throwable("ERROR SET MONEY"))
                        }
                    }
        }
    }

    fun resetPlayer(player: IPlayer): Completable {
        return Completable.create { emitter ->
            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(player.getPlayerName())
                    .setValue(player)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable("ERROR RESET PLAYER"))
                        }
                    }
        }
    }

}