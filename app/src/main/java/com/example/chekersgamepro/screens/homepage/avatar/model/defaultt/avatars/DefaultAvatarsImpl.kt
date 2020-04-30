package com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar

import android.graphics.Bitmap
import android.util.Log
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarState
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.adapters.DefaultAvatarAdapter
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class DefaultAvatarsImpl(
        private val avatarViewModel: AvatarViewModel
        , isMovePage: Observable<Boolean>
        , saveImage: Observable<Boolean>) : IDefaultAvatars, CheckersFragment.LifecycleListener {

    private val compositeDisposable = CompositeDisposable()

    private val defaultAvatarsList = BehaviorSubject.create<List<IDefaultAvatar>>()

    init {
        Log.d("TEST_GAME", "1 DefaultAvatarsImpl -> init ")

        val selectedId = BehaviorSubject.createDefault(-1)
        val movePage = isMovePage.doOnNext { selectedId.onNext(-1) }

        compositeDisposable.add(avatarViewModel.getDefaultAvatarsList()
                .subscribeOn(Schedulers.io())
                .map { defaultAvatarsImageList ->
                    Log.d("TEST_GAME", "2 DefaultAvatarsImpl -> init defaultAvatarsImageList size: ${defaultAvatarsImageList.size}")

                    val defaultAvatarsList = ArrayList<IDefaultAvatar>()
                    for ((index, image) in defaultAvatarsImageList.withIndex()) {

                        val defaultAvatar: IDefaultAvatar =
                                DefaultAvatarImpl(image, selectedId, index, saveImage, movePage, Consumer { createAvatarDataByClick(it) })

                        defaultAvatarsList.add(defaultAvatar)
                    }
                    defaultAvatarsList
                }
                .subscribe { defaultAvatarsList, t2 ->
                    this.defaultAvatarsList.onNext(defaultAvatarsList)
                }
        )
    }

    private fun createAvatarDataByClick(bitmap: Bitmap){
        avatarViewModel.setChangeAvatarData(AvatarData(AvatarState.CAPTURE_AVATAR, bitmap))
    }

    override fun getAvatarsDefaultAdapter(): Single<DefaultAvatarAdapter>? =
            this.defaultAvatarsList.hide()
                    .map {  DefaultAvatarAdapter(it) }
                    .firstOrError()

    override fun onDestroy() {
        compositeDisposable.clear()
    }
}