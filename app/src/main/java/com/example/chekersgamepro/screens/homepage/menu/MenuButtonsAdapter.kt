package com.example.chekersgamepro.screens.homepage.menu

import android.view.ViewGroup
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton

class MenuButtonsAdapter(private val buttonsList: List<IMenuButton>,
                         private val measuredHeight: Int)
    : CheckersRecyclerView.Companion.Adapter<IMenuButton>(buttonsList)  {
    override fun getItem(position: Int) = this.buttonsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = addViewHolder(MenuButtonViewHolder(parent))

    override fun getScreenHeight() = this.measuredHeight

    override fun getFactorHeight() = this.buttonsList.size.toFloat()

    override fun isNeedChangeItemSize() = true

}