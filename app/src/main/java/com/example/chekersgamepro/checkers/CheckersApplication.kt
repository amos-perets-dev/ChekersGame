package com.example.chekersgamepro.checkers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.chekersgamepro.R
import com.google.android.material.internal.ContextUtils
import com.google.firebase.FirebaseApp
import io.realm.Realm
import io.realm.RealmConfiguration

class CheckersApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        FirebaseApp.initializeApp(this)
//       val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.music)
//
//        mediaPlayer.start()
        val realmConfiguration = RealmConfiguration.Builder()
                .name("abb.realm")
                .encryptionKey(ByteArray(64))
                .schemaVersion(32)
                .build()

        Realm.setDefaultConfiguration(realmConfiguration)

        checkersApplication = applicationContext as CheckersApplication?

        CheckersConfiguration.create(checkersApplication?.applicationContext)
    }

    @SuppressLint("RestrictedApi")
    fun getActivityByContext(context: Context): Activity? = when (context) {
        is Activity -> context
        is ContextWrapper -> ContextUtils.getActivity(context.baseContext)
        else -> null
    }



    fun closeAppNow(msgLog: String = "") {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        Log.v("***ERROR***", msgLog)
        checkersApplication?.startActivity(homeIntent)
        android.os.Process.killProcess(android.os.Process.myPid())

    }

    fun showToast(msg: String) {
        Toast.makeText(checkersApplication, msg, Toast.LENGTH_SHORT).show()
    }

    fun getInteger(resId : Int) = applicationContext!!.resources.getInteger(resId)

    fun getColorRes(resId : Int) = applicationContext!!.resources.getColor(resId)


    companion object Factory {

        private var checkersApplication: CheckersApplication? = null

        @JvmStatic
        fun create(): CheckersApplication {
            if (checkersApplication == null) {
                checkersApplication = CheckersApplication()
            }
            return checkersApplication as CheckersApplication
        }
    }

}