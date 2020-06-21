package com.example.chekersgamepro.screens.homepage.topplayers

import android.view.ViewGroup
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer

class TopPlayersAdapter :
        CheckersRecyclerView.Companion.Adapter<ITopPlayer>(){

    private var listTopPlayers = ArrayList<ITopPlayer>()

    private var height : Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = super.addViewHolder(TopPlayerViewHolder(parent, CheckersImageUtil.create()))

    override fun getFactorHeight() = 8f

    override fun isNeedChangeItemSize() = true

    override fun getScreenHeight() = this.height

    fun setScreenHeight(height : Int){
        this.height = height
    }

    override fun getItem(position: Int) = this.listTopPlayers[position]

}