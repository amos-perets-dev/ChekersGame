package com.example.chekersgamepro.screens.homepage.menu.settings

import android.graphics.Color
import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.settings.model.ILanguageItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.language_item.view.*

class LanguageViewHolder(parent: ViewGroup) :
        CheckersRecyclerView.Companion.ViewHolder<ILanguageItem>(parent, R.layout.language_item) {


    init {

    }

    override fun bindData(model: ILanguageItem) {
        super.bindData(model)

        val textViewLanguage = itemView.text_view_language
        val flagIcon = itemView.flag_icon

        flagIcon.setBackgroundResource(model.getIcon())
        textViewLanguage.text = model.getName()

        flagIcon.setOnClickListener { model.onClickItem(adapterPosition) }
        itemView.setOnClickListener { model.onClickItem(adapterPosition) }
        textViewLanguage.setOnClickListener { model.onClickItem(adapterPosition) }


        model.isSelected()
                ?.subscribe {isSelected ->
                    if (isSelected){
                        itemView.setBackgroundColor(Color.parseColor("#79E3E3E3"))
                    } else {
                        itemView.setBackgroundColor(Color.TRANSPARENT)
                    }

                }

    }

    private fun notifyClick() {
//        onClickItem.onNext(adapterPosition)
    }

}