package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.ViewPagerManager
import com.example.chekersgamepro.screens.homepage.avatar.adapters.DefaultAvatarAdapter
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.DefaultAvatarsImpl
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.grid_default_avatars_item.view.*

class AvatarDefaultFragment(private val avatarViewModel: AvatarViewModel) : AvatarFragmentBase(avatarViewModel), CheckersFragment.FragmentLifecycle {

    companion object {
        fun newInstance(avatarViewModel: AvatarViewModel): AvatarDefaultFragment {
            return AvatarDefaultFragment(avatarViewModel)
        }
    }

    override fun getLayoutResId(): Int = R.layout.grid_default_avatars_item

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveImage = avatarViewModel.saveImage(activity!!)
        val defaultAvatars = DefaultAvatarsImpl(avatarViewModel, isMovePage(), saveImage)

        compositeDisposableOnDestroyed.add(
                defaultAvatars.getAvatarsDefaultList()
                        ?.subscribeOn(Schedulers.io())
                        ?.doOnEvent { defaultAvatarAdapter, t2 -> addListener(defaultAvatarAdapter) }
                        ?.subscribe { defaultAvatarAdapter, t2 -> initRecyclerView(defaultAvatarAdapter) }!!
        )
    }

    private fun initRecyclerView(defaultAvatarAdapter: DefaultAvatarAdapter) {

        val recyclerDefaultAvatars = view!!.recycler_default_avatars
        recyclerDefaultAvatars.layoutManager = GridLayoutManager(view!!.context, 3)
        recyclerDefaultAvatars.adapter = defaultAvatarAdapter
    }

}