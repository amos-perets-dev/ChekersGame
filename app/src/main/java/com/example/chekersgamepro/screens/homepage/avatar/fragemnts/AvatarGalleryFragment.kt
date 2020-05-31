package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.getColor
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.personal_avatar_gallery_fragment.view.*

class AvatarGalleryFragment(private val avatarViewModel: AvatarViewModel) :
        AvatarFragmentBase(avatarViewModel), CheckersFragment.FragmentLifecycle {

    private val imageUtil = CheckersImageUtil.create()

    private lateinit var textButtonGallery: AppCompatTextView

    companion object {
        fun newInstance(viewModel: AvatarViewModel): AvatarGalleryFragment {
            return AvatarGalleryFragment(viewModel)
        }

        private const val OPEN_GALLERY_REQUEST = 1111
        private const val ANIMATE_GALLERY_BUTTON_DURATION = 400L
        private const val CANCEL_ANIMATE_GALLERY_BUTTON_DURATION = 0L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textButtonGallery = view.text_button_gallery

        compositeDisposableOnDestroyed.addAll(
                avatarViewModel.isNeedAnimateGalleryButton(this)
                        .subscribeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            animateButtonGallery(
                                    ((-textButtonGallery.left * 1.75).toFloat())
                                    , ((-textButtonGallery.top * 1.65).toFloat())
                                    , 0.35f)
                        }
                        .subscribe(),

                avatarViewModel
                        .getImageProfileTmpChange(this)
                        .subscribeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            AnimationUtil.animateTransitionAlpha(view, it)
                        },

                avatarViewModel
                        .getOpenGalleryText(this)
                        .subscribe { textButtonGallery.text  = it },

                avatarViewModel
                        .openGallery(this)
                        .subscribe {
                            startActivityForResult(it, OPEN_GALLERY_REQUEST)
                        },

                RxView.clicks(textButtonGallery)
                        .subscribe { avatarViewModel.clickOnGallery(context!!) },

                avatarViewModel
                        .saveImage(activity!!)
                        .distinctUntilChanged()
                        .subscribe { textButtonGallery.visibility = View.GONE }
        )
    }

    private fun animateButtonGallery(translationX: Float, translationY: Float, scale: Float, duration: Long = ANIMATE_GALLERY_BUTTON_DURATION) {
        AnimationUtil.translateWithScale(textButtonGallery, duration, translationX, translationY, scale, Consumer {  })
    }

    override fun getLayoutResId(): Int {
        return R.layout.personal_avatar_gallery_fragment
    }

    private fun clearBackground() {
        textButtonGallery.translationX = 0f
        textButtonGallery.translationY = 0f
        textButtonGallery.scaleX = 1f
        textButtonGallery.scaleY = 1f
//        animateButtonGallery(0f, 0f, 1f, CANCEL_ANIMATE_GALLERY_BUTTON_DURATION)
        view!!.setBackgroundColor(getColor(context!!, R.color.activity_home_page_avatar_gallery_background_color))
    }

    override fun onPauseFragment() {
        Log.d("TEST_GAME", "1 AvatarGalleryFragment -> onPauseFragment")

        clearBackground()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_GALLERY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                avatarViewModel.setDataFromGallery(data)
            }
        }
    }
}