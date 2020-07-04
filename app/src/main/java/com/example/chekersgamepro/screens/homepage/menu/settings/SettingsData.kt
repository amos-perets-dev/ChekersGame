package com.example.chekersgamepro.screens.homepage.menu.settings

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class SettingsData(@PrimaryKey var id: Int = 0,
                               var language: Int = 0,
                               var isSound: Boolean = true,
                               var isChat: Boolean = true) : RealmObject()