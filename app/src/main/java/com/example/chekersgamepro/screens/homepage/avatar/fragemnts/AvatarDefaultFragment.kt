package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.adapters.DefaultAvatarAdapter
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.DefaultAvatarsImpl
import com.example.chekersgamepro.views.custom.CustomViewPager
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.grid_default_avatars_item.view.*


class AvatarDefaultFragment(private val avatarViewModel: AvatarViewModel
                            , private val avatarImageTmp: CircleImageView
                            , private val viewPager: CustomViewPager
                            , private val recyclerButtons: RecyclerView)
    : AvatarFragmentBase(avatarViewModel), CheckersFragment.FragmentLifecycle {

    private lateinit var recyclerDefaultAvatars: RecyclerView

    companion object {
        fun newInstance(avatarViewModel: AvatarViewModel, avatarImageTmp: CircleImageView, viewPager: CustomViewPager, recyclerButtons: RecyclerView): AvatarDefaultFragment {
            return AvatarDefaultFragment(avatarViewModel, avatarImageTmp, viewPager, recyclerButtons)
        }
    }

    override fun getLayoutResId(): Int = R.layout.grid_default_avatars_item

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveImage = avatarViewModel.saveImage(activity!!)
        val defaultAvatars = DefaultAvatarsImpl(avatarViewModel, isMovePage(), saveImage)

        compositeDisposableOnDestroyed.add(
                defaultAvatars.getAvatarsDefaultAdapter()
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.doOnEvent { defaultAvatarAdapter, t2 -> addListener(defaultAvatarAdapter) }
                        ?.subscribe { defaultAvatarAdapter, t2 -> initRecyclerView(defaultAvatarAdapter) }!!
        )
    }

    private fun initRecyclerView(defaultAvatarAdapter: DefaultAvatarAdapter) {

        val recyclerDefaultAvatars = view!!.recycler_default_avatars
        recyclerDefaultAvatars.setHasFixedSize(true)
        recyclerDefaultAvatars.setItemViewCacheSize(20)
        recyclerDefaultAvatars.isDrawingCacheEnabled = true
        recyclerDefaultAvatars.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        recyclerDefaultAvatars.layoutManager = GridLayoutManager(view!!.context, 3)
        recyclerDefaultAvatars.adapter = defaultAvatarAdapter
    }

}