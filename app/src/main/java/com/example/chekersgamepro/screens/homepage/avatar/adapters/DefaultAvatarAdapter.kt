package com.example.chekersgamepro.screens.homepage.avatar.adapters

import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.IDefaultAvatar
import com.example.chekersgamepro.screens.homepage.avatar.viewholder.defavatar.DefaultAvatarBaseViewHolder

class DefaultAvatarAdapter(private val defaultAvatarsList: List<IDefaultAvatar>) :
        CheckersRecyclerView.Companion.Adapter<IDefaultAvatar>(defaultAvatarsList), CheckersFragment.LifecycleListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckersRecyclerView.Companion.ViewHolder<IDefaultAvatar> {
        return when (viewType) {
            HIDE_ITEM -> super.addViewHolder(DefaultAvatarBaseViewHolder(parent, R.layout.avatar_default_empty_item))
            else -> super.addViewHolder(DefaultAvatarBaseViewHolder(parent))
        }
    }

    override fun getItemCount(): Int = defaultAvatarsList.size

    override fun getItem(position: Int): IDefaultAvatar = defaultAvatarsList[position]

    override fun getItemViewType(position: Int): Int {
        return if ((defaultAvatarsList.size / 2) == position) HIDE_ITEM else SHOW_ITEM
    }

    override fun onDestroy() {
        super.dispose()
    }

    companion object {
        private const val HIDE_ITEM = 0
        private const val SHOW_ITEM = 1
    }

}