package com.example.chekersgamepro.screens.homepage.avatar.model.button.button

import android.util.Log
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class ButtonAvatarSelectedImpl(private val drawableResId: Int
                               , private val infoScrollPageData: Observable<ScrollPageData>
                               , private val id: Int
                               , private val consumerClick : Consumer<Int>): IButtonAvatarSelected {

    override fun onClick() {
        consumerClick.accept(this.id)
    }

    override fun isStartsSelected() = this.id == 0

    override fun getDrawbleResId(): Int  = this.drawableResId

    override fun getId(): Int = this.id

    override fun isSelected(): Observable<Boolean> {
        return infoScrollPageData
                .filter { it.newPosition == id }
                .map { it.rightDirection }
    }

    override fun isUnSelected(): Observable<Boolean> {
        return infoScrollPageData
                .filter { it.oldPosition == id }
                .map { it.rightDirection }
    }

}