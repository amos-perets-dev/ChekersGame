package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.getColor
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.screens.homepage.avatar.AvatarState
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData
import com.example.chekersgamepro.util.IntentUtil
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.personal_avatar_gallery_item.view.*
import java.io.FileNotFoundException

class AvatarGalleryFragment(private val avatarViewModel: AvatarViewModel) :
        AvatarFragmentBase(avatarViewModel), CheckersFragment.FragmentLifecycle {

    private val imageUtil = CheckersImageUtil.create()

    private lateinit var textButtonGallery: AppCompatTextView

    companion object {
        fun newInstance(viewModel: AvatarViewModel): AvatarGalleryFragment {
            return AvatarGalleryFragment(viewModel)
        }

        private const val OPEN_GALLERY_REQUEST = 1111
        private const val ANIMATE_GALLERY_BUTTON_DURATION = 500L
        private const val CANCEL_ANIMATE_GALLERY_BUTTON_DURATION = 0L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textButtonGallery = view.text_button_gallery

        val isNeedAnimateGalleryButton = avatarViewModel.isNeedAnimateGalleryButton(this)
                .subscribeOn(Schedulers.io())

        compositeDisposableOnDestroyed.addAll(
                isNeedAnimateGalleryButton
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
                        .map { imageUtil.blurBitmapFromBitmap(it) }
                        .map { imageUtil.bitmapToDrawable(it) }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setBackgroundDrawable),

                RxView.clicks(textButtonGallery)
                        .map { IntentUtil.createOpenGalleryIntent() }
                        .subscribe { startActivityForResult(it, OPEN_GALLERY_REQUEST) },

                avatarViewModel
                        .saveImage(activity!!)
                        .distinctUntilChanged()
                        .subscribe { textButtonGallery.visibility = View.GONE }
        )
    }

    private fun animateButtonGallery(translationX: Float, translationY: Float, scale: Float, duration: Long = ANIMATE_GALLERY_BUTTON_DURATION) {
        AnimationUtil.translateWithScale(textButtonGallery, duration, translationX, translationY, scale)
    }

    override fun getLayoutResId(): Int {
        return R.layout.personal_avatar_gallery_item
    }

    private fun clearBackground(){
        textButtonGallery.translationX = 0f
        textButtonGallery.translationY = 0f
        textButtonGallery.scaleX = 1f
        textButtonGallery.scaleY = 1f
//        animateButtonGallery(0f, 0f, 1f, CANCEL_ANIMATE_GALLERY_BUTTON_DURATION)
        view!!.setBackgroundColor(getColor(context!!, R.color.activity_home_page_avatar_gallery_background_color))
    }

    override fun onPauseFragment() {
        clearBackground()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_GALLERY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val imageUri = data?.data
                    val imageStream = activity!!.contentResolver.openInputStream(imageUri!!)

                    avatarViewModel.setChangeAvatarData(
                            AvatarData(AvatarState.CAPTURE_AVATAR, imageUtil.compressBitmap(BitmapFactory.decodeStream(imageStream))))

                } catch (ex: FileNotFoundException) {
                    ex.printStackTrace()
                }
            }
        }
    }
}