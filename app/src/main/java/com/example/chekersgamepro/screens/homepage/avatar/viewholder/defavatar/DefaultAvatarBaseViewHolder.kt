package com.example.chekersgamepro.screens.homepage.avatar.viewholder.defavatar

import android.util.Log
import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.IDefaultAvatar
import com.example.chekersgamepro.views.custom.circle.CircleAngleAnimation
import com.example.chekersgamepro.views.custom.circle.CircleImageViewCustom
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.avatar_default_item.view.*


open class DefaultAvatarBaseViewHolder(parent: ViewGroup, layoutId : Int = R.layout.avatar_default_item) :
        CheckersRecyclerView.Companion.ViewHolder<IDefaultAvatar>(parent, layoutId) {

    private val avatarDefaultImageView= itemView.avatar_default_image

    private val circleImageAnimation= CircleAngleAnimation(avatarDefaultImageView)

    override fun bindData(model: IDefaultAvatar) {

        compositeDisposable.addAll(

                initAnimation()
                        .observeOn(Schedulers.io())
                        .subscribe(),

                RxView.clicks(itemView)
                        .observeOn(Schedulers.io())
                        .subscribe { model.click() },

                model.isSelected()
                        .observeOn(Schedulers.io())
                        .filter(Functions.equalsWith(true))
                        .doOnNext { setDuration(400) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(this::drawOrRemoveCircle)
                        .subscribe (),

                model.isSelected()
                        .observeOn(Schedulers.io())
                        .firstOrError()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { isSelected, t2 ->
                            setDuration(0)
                            drawOrRemoveCircle(isSelected)
                        },

                model.avoidClick()
                        .distinctUntilChanged()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { itemView.isEnabled = false }
                        .subscribe(),

                model.isClearSelected()
                        .doOnNext { Log.d("TEST_GAME", "DefaultAvatarViewHolder -> isClearSelected") }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(this::drawOrRemoveCircle)
                        .subscribe(),

                model.getAvatarDefaultImage()
                        .subscribeOn(Schedulers.io())
                        .subscribe { avatarDefaultImage, t2 -> avatarDefaultImageView.setImageBitmap(avatarDefaultImage) }
        )
    }

    private fun initAnimation(): Completable {
        return Completable.create {
            it.onComplete()
            avatarDefaultImageView.startAnimation(circleImageAnimation)
        }
    }

    private fun setDuration(duration: Long) {
        circleImageAnimation.setAnimateDuration(duration)
    }

    private fun drawOrRemoveCircle(isSelected: Boolean) {
        circleImageAnimation.setAngle(isSelected)
        avatarDefaultImageView.startAnimation(circleImageAnimation)
    }

    override fun unBindData(model: IDefaultAvatar) {
        super.unBindData(model)
        compositeDisposable.clear()
    }

    override fun destroy() {
        super.destroy()
        compositeDisposable.clear()
    }
}