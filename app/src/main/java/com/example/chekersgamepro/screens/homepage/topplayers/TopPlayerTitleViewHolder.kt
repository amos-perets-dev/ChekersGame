package com.example.chekersgamepro.screens.homepage.topplayers

import android.graphics.Color
import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer
import kotlinx.android.synthetic.main.top_player_item.view.*
import kotlinx.android.synthetic.main.top_player_title_item.view.*

class TopPlayerTitleViewHolder(parent : ViewGroup) :
        CheckersRecyclerView.Companion.ViewHolder<ITopPlayer>(parent, R.layout.top_player_title_item)