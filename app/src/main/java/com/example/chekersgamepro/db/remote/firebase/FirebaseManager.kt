package com.example.chekersgamepro.db.remote.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.PlayerImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.util.NetworkUtil
import com.google.common.base.Optional
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class FirebaseManager {

    private val databaseUsers = FirebaseDatabase.getInstance().getReference("Users")
    private val databasePlayers = FirebaseDatabase.getInstance().getReference("CenterPlayers")

    private val networkUtil = NetworkUtil()


    fun addNewUser(user: IUserProfile): Single<Boolean> {
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

    fun addNewPlayer(player: IPlayer): Single<Boolean> {

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

    fun isUserNameExist(userName: String): Single<Boolean> {
        val applesQuery = databaseUsers.child(userName)

        return RxFirebaseDatabase.data(applesQuery)
                .map(DataSnapshot::getChildren)
                .map { it.toSet() }
                .map { it.size }
                .flatMap { Single.just(it != 0) }
    }

    fun setIsCanPlay(playerName: String, level: String, isCanPlay: Boolean): Completable {
        Log.d("TEST_GAME", "PLAYER NAME: $playerName")

        return Completable.create { emitter ->
            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("canPlay")
                    .setValue(isCanPlay)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TEST_GAME", "FirebaseManager -> setIsCanPlay")
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            Log.d("TEST_GAME", "FirebaseManager -> onError -> task.exception.toString()")
                            emitter.onError(Throwable("ERROR SET CAN PLAY"))
                        }
                    }
        }
    }

    fun getRequestStatusChanges(playerName: String, level: String): Observable<RequestOnlineGameStatus> {
        val query = databasePlayers
                .child(level)
                .child(playerName)
                .child("requestOnlineGameStatus")

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(RequestOnlineGameStatus::class.java) }

    }

    fun getPlayerChanges(playerName: String, level: String): Observable<IPlayer> {
        val query = databasePlayers.child(level).child(playerName)

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(PlayerImpl::class.java) }
                .cast(IPlayer::class.java)

    }

    private fun deletePlayer(player: PlayerImpl, isNeedUpdateLevel: Boolean): Single<Boolean> {

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

    fun acceptOnlineGame(remotePlayerName: String, playerName: String, level: String): Completable {

        return Completable.create { emitter ->
            databasePlayers
                    .child(level)
                    .child(remotePlayerName)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.ACCEPT_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable())
                        }
                    }

            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.ACCEPT_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable())
                        }
                    }
        }


    }

    fun declineOnlineGame(remotePlayer: String, player: IPlayer): Completable {

        return Completable.create { emitter ->

            // The remote player is the owner
            // that need to notify on the decline game
            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(remotePlayer)
                    .child("requestOnlineGameStatus")
                    .setValue(RequestOnlineGameStatus.DECLINE_BY_GUEST)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            emitter.onError(Throwable("ERROR DECLINE GAME"))
                        }
                    }

            databasePlayers
                    .child(player.getLevelPlayer().toString())
                    .child(player.getPlayerName())
                    .setValue(player)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable("ERROR DECLINE GAME"))
                        }
                    }

        }

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
                .filter { it.value is Boolean }
                .map { it.value as Boolean }
    }

    fun setMoney(userName: String, money: Int): Completable {
        Log.d("TEST_GAME", "FirebaseManager -> setMoney:  fun setMoney(userName: String: $userName")

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

    fun storeImageToFirebase(image: Bitmap?, playerName: String): Single<String> = Single.create { emitter ->
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://checkers-d0d7a.appspot.com/")
        val mountainImagesRef = storageRef.child("Avatars/$playerName.jpg")
        val baos = ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        val uploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->

            val ref = FirebaseStorage.getInstance().reference.child(taskSnapshot.metadata?.path!!)
            ref.downloadUrl.addOnSuccessListener {
                val imageUrl = it.toString()
                emitter.onSuccess(imageUrl)
            }

        }.addOnFailureListener { emitter.onError(Throwable(it.message)) }
    }

    fun setImageProfileAndPlayer(playerName: String, level: String, encodeImage: String): Completable {
        return Completable.create { emitter ->
            databasePlayers
                    .child(level)
                    .child(playerName)
                    .child("encodeImage")
                    .setValue(encodeImage)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            emitter.onError(Throwable("ERROR SET IMAGE PROFILE URL"))
                        }
                    }

            databaseUsers
                    .child(playerName)
                    .child("encodeImage")
                    .setValue(encodeImage)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable("ERROR SET IMAGE PROFILE URL"))
                        }
                    }
        }
    }

    fun setImageDefaultPreUpdate(): Single<ByteArray?> {

        val firebaseStorage = FirebaseStorage.getInstance()

        return Single.create { emitter ->
            // Create a storage reference from our app
            // Create a storage reference from our app
            val storageRef: StorageReference = firebaseStorage.reference


            /*In this case we'll use this kind of reference*/
            //Download file in Memory
            /*In this case we'll use this kind of reference*/ //Download file in Memory
            val islandRef = storageRef.child("AvatarsPlayersDefault/default_avatar_icon.jpg")

            val ONE_MEGABYTE = 512 * 512.toLong()
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { imageByteArray ->

                emitter.onSuccess(imageByteArray)
            }.addOnFailureListener {
                emitter.onError(Throwable())
                // Handle any errors
            }
        }
    }


}