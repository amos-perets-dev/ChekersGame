package com.example.chekersgamepro.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import io.realm.Realm
import io.realm.RealmConfiguration

class CheckersApplication : MultiDexApplication(){

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        FirebaseApp.initializeApp(this)

        val realmConfiguration = RealmConfiguration.Builder()
                .name("k.realm")
                .encryptionKey(ByteArray(64))
                .schemaVersion(11)
                .build()

        Realm.setDefaultConfiguration(realmConfiguration)

        checkersApplication = applicationContext as CheckersApplication?
    }

    fun closeAppNow(msgLog: String = "") {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        Log.v("***ERROR***", msgLog)
        checkersApplication?.startActivity(homeIntent)
        android.os.Process.killProcess(android.os.Process.myPid())

    }

    fun showToast(msg : String){
        Toast.makeText(checkersApplication, msg, Toast.LENGTH_SHORT).show()
    }

    companion object Factory{

        private var checkersApplication : CheckersApplication? = null

        @JvmStatic
        fun create() : CheckersApplication{
            if(checkersApplication == null){
                checkersApplication = CheckersApplication()
            }
            return checkersApplication as CheckersApplication
        }
    }

}