package com.example.chekersgamepro.screens.homepage.menu.settings.model

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class LanguageItem(
        private val name: String = "",
        private val icon: Int = 0,
        private val onClickItem : BehaviorSubject<ILanguageItem>,
        private var position : Int = -1
) : ILanguageItem {

    override fun getName() = this.name
    override fun getPosition() = this.position

    override fun setPosition(position: Int) {
        this.position = position
    }

    override fun getIcon() = this.icon

    override fun isSelected(): Observable<Boolean> {
       return onClickItem
                .distinctUntilChanged()
                .map { it.getName() == this.name }
    }

    override fun onClickItem(adapterPosition: Int) {
        onClickItem.onNext(this)
    }

    override fun equals(other: Any?): Boolean {
        if(other is LanguageItem){
            return name == other.getName()
        }

        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + icon
        result = 31 * result + onClickItem.hashCode()
        return result
    }
}