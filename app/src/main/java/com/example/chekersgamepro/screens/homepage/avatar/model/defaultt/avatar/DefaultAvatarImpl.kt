package com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar

import android.graphics.Bitmap
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class DefaultAvatarImpl(
        private val bitmap: Bitmap
        , private val selectedId: BehaviorSubject<Int>
        , private val id: Int
        , private val avoidClick: Observable<Boolean>
        , private val moveNextPage: Observable<Boolean>
        , private val consumerClick : Consumer<Bitmap>): IDefaultAvatar {

    override fun avoidClick(): Observable<Boolean> = avoidClick

    override fun getAvatarDefaultImage(): Single<Bitmap> = Single.just(this.bitmap)

    override fun isSelected(): Observable<Boolean> =
            selectedId.hide()
                    .map{selectedId -> selectedId == this.id}
                    .distinctUntilChanged()

    override fun isNotSelected(): Boolean {
        return selectedId.value != this.id
    }

    override fun click() {
        if (isNotSelected()){
            selectedId.onNext(this.id)
            consumerClick.accept(this.bitmap)
        }
    }

    override fun isClearSelected() : Observable<Boolean>{
        val movePage = moveNextPage
                .subscribeOn(Schedulers.io())
                .map { selectedId.value == this.id }
                .startWith(true)
                .filter(Functions.equalsWith(true))

        val isNotSelected =
                isSelected()
                        .subscribeOn(Schedulers.io())
                        .filter(Functions.equalsWith(false))
                        .doOnNext { Log.d("TEST_GAME", "isClearSelected: $it, $id") }


        return Observable.combineLatest(
                movePage
                , isNotSelected
                , BiFunction { movePage : Boolean, isNotSelected : Boolean -> false })

    }
}