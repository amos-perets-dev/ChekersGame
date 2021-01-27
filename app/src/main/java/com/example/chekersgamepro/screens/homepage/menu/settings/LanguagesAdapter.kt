package com.example.chekersgamepro.screens.homepage.menu.settings

import android.view.ViewGroup
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.settings.model.ILanguageItem
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LanguagesAdapter(private val listLanguages: ArrayList<ILanguageItem>) : CheckersRecyclerView.Companion.Adapter<ILanguageItem>(listLanguages) {

    private val onClickItem = PublishSubject.create<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            super.addViewHolder(LanguageViewHolder(parent))


    override fun isNeedChangeItemSize() = false

    override fun isFirstItemNeedChangeItemSize() = false

    override fun getItem(position: Int) = this.listLanguages[position]

    fun getOnClickItem(): Observable<Int> = onClickItem.hide()
}