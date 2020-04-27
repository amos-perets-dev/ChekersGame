package com.example.chekersgamepro.db.localy.preferences

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.chekersgamepro.checkers.CheckersApplication
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesManager(private val context: CheckersApplication) {

    private val prefs = this.context.getSharedPreferences("com.example.chekersgamepro2", AppCompatActivity.MODE_PRIVATE)

    fun isFirstTimeRun(): Boolean = prefs.getBoolean("first_run", true)

    fun isRegistered(): Boolean{
        val isRegisted = prefs.getBoolean("is_registered", false)
        Log.d("TEST_GAME", "SharedPreferencesManager -> isRegistered: $isRegisted")
        return isRegisted
    }


    fun setRunFirstTime() {
        if (prefs.getBoolean("first_run", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("first_run", false).apply()
        }
    }

    fun setIsRegistered() {
        Log.d("TEST_GAME", "SharedPreferencesManager -> setIsRegistered")
        prefs.edit().putBoolean("is_registered", true).apply()
    }

    fun isDefaultImage(): Boolean{
        val isRegisted = prefs.getBoolean("is_default_image", true)
        return isRegisted
    }

    fun setIsDefaultImage(): Completable{
        prefs.edit().putBoolean("is_default_image", false).apply()
        return Completable.complete()
    }

    fun setEncodeImageDefaultPreUpdate(encodeBase64Image: String?): Single<Boolean> {
        prefs.edit().putString("encode_image", encodeBase64Image).apply()
        return Single.just(true)
    }

    fun getEncodeImageDefaultPreUpdate(): String? =  prefs.getString("encode_image", "")


}