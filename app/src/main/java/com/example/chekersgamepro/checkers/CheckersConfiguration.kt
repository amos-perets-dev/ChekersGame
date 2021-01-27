package com.example.chekersgamepro.checkers

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.settings.SettingsData
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.BehaviorSubject
import io.realm.Realm

class CheckersConfiguration(private val context: Context) {

    private val imageUtil = CheckersImageUtil.create()
    private val resources: Resources = context.resources

    private val avatarList = ArrayList<Bitmap>()

    private val avatarImagesList = BehaviorSubject.create<List<Bitmap>>()

    private val computerIconEncode: String


    init {

        val bitmap = context.getDrawable(R.drawable.ic_robot_circle)?.let { imageUtil.drawableToBitmap(it) }
        this.computerIconEncode = bitmap?.let { imageUtil.encodeBase64Image(it).toString() }.toString()

    }

    fun getComputerIconEncode() = this.computerIconEncode

    fun getDefaultAvatarsList(): Single<List<Bitmap>>? {
        return avatarImagesList.hide().firstOrError()
    }

    public fun initAvatarList(): Single<Boolean> {
        return Single.create<Boolean> { emitter ->
            Log.d("TEST_GAME", "1 initAvatarList")

            emitter.onSuccess(true)

            Log.d("TEST_GAME", "2 initAvatarList")

            if (avatarList.isEmpty()) {
                val bitmap1 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_1))
                val bitmap2 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_2))
                val bitmap3 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_3))
                val bitmap4 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_4))
                val bitmap5 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_9))
                val bitmap6 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_6))
                val bitmap7 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_7))
                val bitmap8 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_8))
                val bitmap9 = imageUtil.drawableToBitmap(resources.getDrawable(R.drawable.avatar_5))

                avatarList.add(bitmap1)
                avatarList.add(bitmap2)
                avatarList.add(bitmap3)
                avatarList.add(bitmap4)
                avatarList.add(bitmap5)
                avatarList.add(bitmap6)
                avatarList.add(bitmap7)
                avatarList.add(bitmap8)
                avatarList.add(bitmap9)

                avatarImagesList.onNext(avatarList)
            }
            Log.d("TEST_GAME", "3 initAvatarList")
        }

    }

    fun getDefaultAvatarDrawable() = context.getDrawable(R.drawable.default_avatar_icon)

    companion object Factory {
        private var checkersConfiguration: CheckersConfiguration? = null

        @JvmStatic
        fun create(context: Context?): CheckersConfiguration {
            if (checkersConfiguration == null) {
                checkersConfiguration = context?.let { CheckersConfiguration(it) }
            }

            return checkersConfiguration as CheckersConfiguration
        }

        @JvmStatic
        fun getInstance(): CheckersConfiguration {
            return checkersConfiguration as CheckersConfiguration
        }
    }
}