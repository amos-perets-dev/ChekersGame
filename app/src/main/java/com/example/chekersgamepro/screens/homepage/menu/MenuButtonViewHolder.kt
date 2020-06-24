package com.example.chekersgamepro.screens.homepage.menu

import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.TouchClickListener
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton
import com.example.chekersgamepro.util.TouchListener
import kotlinx.android.synthetic.main.menu_button_item.view.*

class MenuButtonViewHolder(parent: ViewGroup)
    : CheckersRecyclerView.Companion.ViewHolder<IMenuButton>(parent, R.layout.menu_button_item)  {

    init {
//        itemView
//                .setOnTouchListener(TouchClickListener(View.OnClickListener {  getDataModel()?.onClick() }, 0.6f))

        itemView
                .setOnTouchListener(TouchListener(View.OnClickListener {
                    Log.d("TEST_GAME", "onClick")
                    getDataModel()?.onClick() }, 1.1f))

    }

    override fun bindData(model: IMenuButton) {
        super.bindData(model)
        itemView.menu_button.text = model.getButtonName()
    }



}