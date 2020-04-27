package com.example.chekersgamepro.screens.homepage.avatar.adapters

import android.util.Log
import android.view.ViewGroup
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.avatar.model.button.button.IButtonAvatarSelected
import com.example.chekersgamepro.screens.homepage.avatar.viewholder.buttons.ButtonAvatarSelectedBaseViewHolder
import com.example.chekersgamepro.screens.homepage.avatar.viewholder.buttons.ButtonAvatarSelectedFirstViewHolder

class ButtonsAvatarSelectedAdapter(private val buttonsAvatarSelectedList: List<IButtonAvatarSelected>)
    : CheckersRecyclerView.Companion.Adapter<IButtonAvatarSelected>(buttonsAvatarSelectedList), CheckersFragment.LifecycleListener {


    override fun getItem(position: Int): IButtonAvatarSelected = buttonsAvatarSelectedList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CheckersRecyclerView.Companion.ViewHolder<IButtonAvatarSelected> {
        Log.d("TEST_GAME", "ButtonsAvatarSelectedAdapter onCreateViewHolder:  ")

        return when (viewType) {
            FIRST_ITEM -> super.addViewHolder(ButtonAvatarSelectedFirstViewHolder(parent))
            else -> super.addViewHolder(ButtonAvatarSelectedBaseViewHolder(parent))
        }
    }


    override fun onDestroy() {
        Log.d("TEST_GAME", "ButtonsAvatarSelectedAdapter onDestroy:  ")
        super.dispose()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) FIRST_ITEM else OTHER_ITEM
    }

    companion object {
        private const val FIRST_ITEM = 0
        private const val OTHER_ITEM = 1
    }
}
