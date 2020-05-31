package com.example.chekersgamepro.db.remote.firebase

import android.graphics.Bitmap
import android.util.Log
import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.models.player.data.IPlayer
import com.example.chekersgamepro.models.player.data.PlayerImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.util.NetworkUtil
import com.google.common.base.Optional
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream


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

    fun setIsCanPlay(level: String, fieldsMap: HashMap<String, Any>) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR SET CAN PLAY")

    fun finishRequestOnlineGame(fieldsMap: HashMap<String, Any>, level: String) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR FINISH REQUEST GAME")

    fun sendRequestOnlineGame(fieldsMap: HashMap<String, Any>, level: String) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR SEND REQUEST GAME")

    fun acceptOnlineGame(level: String, fieldsMap: HashMap<String, Any>) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR ACCEPT GAME")

    fun declineOnlineGame(fieldsMap: HashMap<String, Any>, levelPlayer: String) =
            setData(databasePlayers.child(levelPlayer), fieldsMap, "ERROR DECLINE GAME")

    fun cancelRequestGame(fieldsMap: HashMap<String, Any>, levelPlayer: String) =
            setData(databasePlayers.child(levelPlayer), fieldsMap, "ERROR CANCEL REQUEST GAME")

    fun notifyMove(level: String, fieldsMap: HashMap<String, Any>) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR NOTIFY MOVE")

    fun pingFinishGameTechnicalLoss(level: String, fieldsMap: HashMap<String, Any>) =
            setData(databasePlayers.child(level), fieldsMap, "TECHNICAL LOSS ERROR")

    fun resetPlayer(level: String, fieldsMap: HashMap<String, Any>) =
            setData(databasePlayers.child(level), fieldsMap, "ERROR RESET PLAYER")

    fun setMoney(fieldsMap: HashMap<String, Any>) =
            setData(databaseUsers, fieldsMap, "ERROR SET MONEY")

    private fun setData(databaseReference: DatabaseReference, fieldsMap: HashMap<String, Any>, exception: String): Completable {
        return Completable.create { emitter ->
            databaseReference
                    .updateChildren(fieldsMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                            //Do what you need to do
                        } else {
                            emitter.onError(Throwable(exception))
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
                    .child("avatarEncode")
                    .setValue(encodeImage)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            emitter.onError(Throwable("ERROR SET IMAGE PROFILE URL"))
                        }
                    }

            databaseUsers
                    .child(playerName)
                    .child("avatarEncode")
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


    fun getRemoteMove(playerName: String, level: String): Observable<RemoteMove> {

        val query = databasePlayers
                .child(level)
                .child(playerName)
                .child("remoteMove")

        return RxFirebaseDatabase.dataChanges(query)
                .map { it.getValue(RemoteMove::class.java) }
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

    fun isUserNameExist(userName: String): Single<Boolean> {
        val applesQuery = databaseUsers.child(userName)

        return RxFirebaseDatabase.data(applesQuery)
                .map(DataSnapshot::getChildren)
                .map { it.toSet() }
                .map { it.size }
                .flatMap { Single.just(it != 0) }
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

    fun getAllPlayersByLevel(levelPlayer: Int): Observable<MutableIterable<DataSnapshot>> {

        val databaseReference = databasePlayers.child(levelPlayer.toString())

        return RxFirebaseDatabase.dataChanges(databaseReference)
                .subscribeOn(Schedulers.io())
                .map { it.children }
    }
}