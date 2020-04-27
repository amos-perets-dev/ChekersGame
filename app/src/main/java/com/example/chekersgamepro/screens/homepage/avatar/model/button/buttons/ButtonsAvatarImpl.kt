package com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons

import android.util.Log
import android.view.View
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.avatar.model.button.button.ButtonAvatarSelectedImpl
import com.example.chekersgamepro.screens.homepage.avatar.model.button.button.IButtonAvatarSelected
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import com.example.chekersgamepro.views.custom.ColorAnimateView
import com.google.common.collect.Lists
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class ButtonsAvatarImpl() : IButtonsAvatar {

    private var buttonAvatarSelectedList = ArrayList<IButtonAvatarSelected>()

    private val buttonAvatarSelectedClick = PublishSubject.create<Int>()


    override fun createButtonAvatarSelectedList(infoScrollPageData: Observable<ScrollPageData>): Single<ArrayList<IButtonAvatarSelected>> {
        return Single.create<ArrayList<IButtonAvatarSelected>> { emitter ->

            val buttonAvatarSelectedCamera: IButtonAvatarSelected =
                    ButtonAvatarSelectedImpl(R.drawable.ic_camera, infoScrollPageData, 0, Consumer { buttonAvatarSelectedClick.onNext(it) })
            val buttonAvatarSelectedDefault: IButtonAvatarSelected =
                    ButtonAvatarSelectedImpl(R.drawable.ic_grid, infoScrollPageData, 1, Consumer { buttonAvatarSelectedClick.onNext(it) })
            val buttonAvatarSelectedGallery: IButtonAvatarSelected =
                    ButtonAvatarSelectedImpl(R.drawable.ic_gallery, infoScrollPageData, 2, Consumer {  buttonAvatarSelectedClick.onNext(it) })

            buttonAvatarSelectedList = Lists.newArrayList(buttonAvatarSelectedCamera, buttonAvatarSelectedDefault, buttonAvatarSelectedGallery)
            emitter.onSuccess(buttonAvatarSelectedList)

        }
    }

    override fun getButtonClick(): Observable<Int> =
            buttonAvatarSelectedClick.hide()
                    .doOnNext { Log.d("TEST_GAME", "getButtonClick") }

}