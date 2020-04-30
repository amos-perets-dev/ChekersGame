package com.example.chekersgamepro.screens.homepage.avatar.viewholder.buttons

import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.avatar.model.button.button.IButtonAvatarSelected
import com.example.chekersgamepro.views.custom.ColorAnimateView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.button_avatar_selected_item.view.*

open class ButtonAvatarSelectedBaseViewHolder(parent: ViewGroup, layoutId : Int = R.layout.button_avatar_selected_item)
    : CheckersRecyclerView.Companion.ViewHolder<IButtonAvatarSelected>(parent, layoutId) {

    private val avatarSelectedButton= itemView.avatar_select_button

    override fun bindData(model: IButtonAvatarSelected) {
        super.bindData(model)

        Log.d("TEST_GAME", "ButtonAvatarSelectedViewHolder bindData")

        avatarSelectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, model.getDrawbleResId(), 0)

        compositeDisposable.addAll(
                RxView.clicks(itemView)
                        .observeOn(Schedulers.io())
                        .subscribe {model.onClick()},

                model.isSelected()
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::animateSelected),

                model.isUnSelected()
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::animateUnSelected)
        )

    }


    private fun animateUnSelected(isRightDirection: Boolean) {
        if (isRightDirection) {
            avatarSelectedButton.animateUnselectedLeftToRight()
        } else {
            avatarSelectedButton.animateUnselectedRightToLeft()
        }
    }

    private fun animateSelected(isRightDirection: Boolean) {
        if (isRightDirection) {
            avatarSelectedButton.animateSelectedLeftToRight()
        } else {
            avatarSelectedButton.animateSelectedRightToLeft()
        }
    }

    override fun unBindData(model: IButtonAvatarSelected) {
        compositeDisposable.clear()
        super.unBindData(model)
    }

    override fun destroy() {
        compositeDisposable.dispose()
        super.destroy()
    }
}


