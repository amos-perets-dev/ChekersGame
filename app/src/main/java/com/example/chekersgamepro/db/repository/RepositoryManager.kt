package com.example.chekersgamepro.db.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.chekersgamepro.db.firebase.FirebaseManager
import com.example.chekersgamepro.models.IUserProfile
import com.example.chekersgamepro.models.UserProfileImpl
import com.example.chekersgamepro.util.CheckersApplication
import io.reactivex.Completable
import io.reactivex.Single

class RepositoryManager {

    private val prefs: SharedPreferences

    private val context = CheckersApplication.create()

    private var userNameExistInvalid = HashSet<String>()

    private var userNameNotExistValid = HashSet<String>()

    private var userProfile: IUserProfile = UserProfileImpl()

    private val firebaseManager = FirebaseManager()

    init {
        prefs = context.getSharedPreferences("com.example.chekersgamepro", AppCompatActivity.MODE_PRIVATE)
    }

    fun isFirstTimeRun(): Boolean {
        return prefs.getBoolean("first_run", true)
    }

    fun isRegistered(): Boolean {
        return prefs.getBoolean("is_registered", false)
    }


    fun setRunFirstTime() {
        if (prefs.getBoolean("first_run", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("first_run", false).apply()
        }
    }

    fun addNewUser(userName: String): Single<Boolean> {
        userProfile.setIsRegistered(true)
        userProfile.setUserName(userName)
        return firebaseManager.addNewUser(userProfile as UserProfileImpl)
                .doOnError {
                    Log.d("TEST_GAME", "1 doOnError")
                }
                .toSingleDefault(true)
                .onErrorReturnItem(false)
                .doOnEvent { isAdded, throwable ->
                    Log.d("TEST_GAME", "1 doOnEvent isAdded: $isAdded")
                    if (isAdded) {
//                    prefs.edit().putBoolean("is_registered", true).apply()
                    }
                }
    }

    fun isUserNameExistLocallyListInvalid(userName: String): Boolean {
        return userNameExistInvalid.contains(userName)
    }

    fun isUserNameExistLocallyListValid(userName: String): Boolean {
        return userNameNotExistValid.contains(userName)
    }

    fun isUserNameExist(userName: String): Single<Boolean> {

        if (isUserNameExistLocallyListInvalid(userName)) {
            Log.d("TEST_GAME", "EXIST LOCAL INVALID")
            return Single.just(true)
        }

        if (isUserNameExistLocallyListValid(userName)) {
            Log.d("TEST_GAME", "EXIST LOCAL VALID")
            return Single.just(false)
        }

        return firebaseManager.isUserNameExist(userName)
                .doOnEvent { isExist, t2 ->
                    Log.d("TEST_GAME", "REMOETLY")
                    if (isExist) {
                        userNameExistInvalid.add(userName)
                    } else {
                        userNameNotExistValid.add(userName)
                    }
                }

    }

    companion object Factory {

        private var repositoryManager: RepositoryManager? = null

        @JvmStatic
        fun create(): RepositoryManager {
            if (repositoryManager == null) {
                repositoryManager = RepositoryManager()
            }
            return repositoryManager as RepositoryManager
        }
    }

}