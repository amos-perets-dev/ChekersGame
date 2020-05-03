package com.example.chekersgamepro.screens.homepage.online

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.checkers.recycler.CheckersRecyclerView
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.IDefaultAvatar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.online_player_item.view.*

class OnlinePlayersViewHolder(parent: ViewGroup) :
        CheckersRecyclerView.Companion.ViewHolder<IOnlinePlayerEvent>(parent, R.layout.online_player_item), View.OnClickListener {

    private val imageProfile = itemView.image_profile

    init {
        itemView.name_player.setOnClickListener(this)
        itemView.level_player.setOnClickListener(this)
    }

    override fun bindData(model: IOnlinePlayerEvent) {
        Log.d("TEST_GAME", "OnlinePlayersViewHolder bindData:  ")

//        itemView.name_player.text = model.getPlayerName()
//        itemView.level_player.text = model.getLevelPlayer()
//        setImageProfile()

        compositeDisposable.addAll(

                model.getPlayerName()
                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{itemView.name_player.text = it},

                model.getLevelPlayer()
                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{itemView.level_player.text  = it},

                        model.getEncodeImageProfile()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(imageProfile::setImageBitmap)
        )
    }

    override fun unBindData(model: IOnlinePlayerEvent) {
        compositeDisposable.clear()
        super.unBindData(model)
    }

    override fun onClick(view: View) {
        val model = getDataModel()
        model?.onClickItem(model)
    }


}