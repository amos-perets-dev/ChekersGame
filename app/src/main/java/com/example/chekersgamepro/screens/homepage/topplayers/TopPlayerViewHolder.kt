package com.example.chekersgamepro.screens.homepage.topplayers

import android.view.ViewGroup
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.top_player_item.view.*

class TopPlayerViewHolder(parent: ViewGroup) :
        CheckersRecyclerView.Companion.ViewHolder<ITopPlayer>(parent, R.layout.top_player_item) {

    override fun bindData(model: ITopPlayer) {
        super.bindData(model)
        itemView.text_view_position.text = model.getPlayerPositionSign()
        itemView.text_view_name_avatar.text = model.getPlayerName()
        itemView.text_view_total_win.text = model.getTotalWin().toString()
        itemView.text_view_total_loss.text = model.getTotalLoss().toString()
        itemView.text_view_money.text = model.getMoney().toString()
        compositeDisposable.add(
                model.getAvatar()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(itemView.image_profile_top_player::setImageBitmap)
        )
    }

    override fun destroy() {
        compositeDisposable.clear()
        super.destroy()
    }

}