package com.example.chekersgamepro.screens.splash

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.registration.RegistrationActivity
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersConfiguration
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashViewModel : ViewModel() {

    private val repositoryManager = RepositoryManager.create()

    private val context = CheckersApplication.create()

    private val compositeDisposable = CompositeDisposable()

    init {
        val checkersConfiguration = CheckersConfiguration.create(context)

        compositeDisposable.add(
                checkersConfiguration
                        .initAvatarList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
        )
    }

    fun getNextClass(): Intent =
            if (repositoryManager.isRegistered()) {
                Intent(context, HomePageActivity::class.java)
            } else {
                Intent(context, RegistrationActivity::class.java)
            }


    fun setRunFirstTime() {
        repositoryManager.setRunFirstTime()
    }

    fun setImageDefaultPreUpdate(): Single<Boolean> =
            if (repositoryManager.isRegistered())
                Single.just(true).delay(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            else repositoryManager.setImageDefaultPreUpdate()

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }


    //
//    private val image = MutableLiveData<Bitmap>()
//
//    init {
//
////        val ref = FirebaseStorage.getInstance().reference.child("AvatarsPlayersDefault/11.jpg")
////        val ref = FirebaseStorage.getInstance().reference.child("AvatarsPlayersDefault/")
//
//        val storageReference = FirebaseStorage.getInstance().reference
//
//        for (i in 1..5){
//            val ref = storageReference.child("AvatarsPlayersDefault/$i.jpg")
//            setImage(ref)
//        }
//
//    }
//
//
//    private fun setImage(ref: StorageReference) {
//        try {
//            val localFile = File.createTempFile("AvatarsPlayersDefault", "jpg")
//            ref.getFile(localFile)
//                    .addOnSuccessListener {
//                        val myImage = BitmapFactory.decodeFile(localFile.absolutePath)
//                        image.postValue(myImage)
//                    }
//                    .addOnFailureListener { e ->
//                        Log.d("TEST_GAME", "ERROR: ${e.message}")
//
//                    }
//
//        } catch (e: IOException) {
//            Log.d("TEST_GAME", "ERROR: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//    fun getImage(lifecycleOwner: LifecycleOwner) : Observable<Bitmap>{
//        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, image))
//    }

//    init {
//
//        val ref = FirebaseStorage.getInstance().reference.child("Avatars/amos.jpg")
//        try {
//            val localFile = File.createTempFile("Avatars", "jpg")
//            ref.getFile(localFile)
//                    .addOnSuccessListener {
//                        val myImage = BitmapFactory.decodeFile(localFile.absolutePath)
//                        image_test.setImageBitmap(myImage)
//                    }
//                    .addOnFailureListener { e ->
//                        Log.d("TEST_GAME", "ERROR: ${e.message}")
//
//                    }
//
//        } catch (e: IOException) {
//            Log.d("TEST_GAME", "ERROR: ${e.message}")
//            e.printStackTrace()
//        }
//
//    }

}