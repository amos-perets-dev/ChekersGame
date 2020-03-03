package com.example.chekersgamepro.db.localy.preferences

import androidx.appcompat.app.AppCompatActivity
import com.example.chekersgamepro.util.CheckersApplication

class SharedPreferencesManager(private val context: CheckersApplication) {

    private val prefs = this.context.getSharedPreferences("com.example.chekersgamepro", AppCompatActivity.MODE_PRIVATE)

    fun isFirstTimeRun(): Boolean = prefs.getBoolean("first_run", true)

    fun isRegistered(): Boolean =  prefs.getBoolean("is_registered", false)


    fun setRunFirstTime() {
        if (prefs.getBoolean("first_run", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("first_run", false).apply()
        }
    }

    fun setIsRegistered() {
        prefs.edit().putBoolean("is_registered", true).apply()
    }

}