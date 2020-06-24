package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.ViewPagerManager
import com.example.chekersgamepro.screens.homepage.avatar.adapters.ButtonsAvatarSelectedAdapter
import com.example.chekersgamepro.util.animation.AnimationUtil
import com.example.chekersgamepro.views.custom.CustomViewPager
import com.jakewharton.rxbinding2.view.RxView
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.fragment_avatar.view.*


class AvatarPickerFragment(private val image_profile_hp: CircleImageView) :
        CheckersFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private lateinit var recyclerButtons: RecyclerView

    private lateinit var actionOkButton: AppCompatButton

    private lateinit var avatarImageTmp: CircleImageView

    private lateinit var viewPager: CustomViewPager

    private val avatarViewModel by lazy {
        ViewModelProviders.of(this).get(AvatarViewModel::class.java)
    }

    companion object {
        fun newInstance(image_profile_hp: CircleImageView): AvatarPickerFragment? {
            Log.d("TEST_GAME", "AvatarPickerFragment -> newInstance")
            return AvatarPickerFragment(image_profile_hp)
        }
    }

    override fun getLayoutResId(): Int = R.layout.fragment_avatar

    private fun animateActionOkButton(it: Float) {
//        if (this.actionOkButton.translationY == it) return
        AnimationUtil.translateY(this.actionOkButton, it, 200)
    }

    private fun animateVisibilityAvatarImageTmp(it: Float) {
//        if (it == this.avatarImageTmp.alpha )return
        AnimationUtil.alpha(this.avatarImageTmp, it, 200)
    }

    private fun animateImageProfile(): Completable {
        val translationY = (avatarImageTmp.y - image_profile_hp.y)
        val translationX = (image_profile_hp.x - avatarImageTmp.x)
        return AnimationUtil.translateXY(
                avatarImageTmp
                , translationX
                , -translationY
                , getInteger(R.integer.activity_home_page_avatar_animate_image_translation).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(this)

        this.avatarImageTmp = view.avatar_image_tmp
        this.actionOkButton = view.action_ok_button
        this.recyclerButtons = view.recycler_buttons_avatar_selected
        this.viewPager = view.avatar_pager_fragment
    }

    private fun initRecyclerViewButtonsAvatarSelected(buttonsAvatarSelectedAdapter: ButtonsAvatarSelectedAdapter) {
        this.recyclerButtons.layoutManager = GridLayoutManager(activity, buttonsAvatarSelectedAdapter.itemCount)
        this.recyclerButtons.adapter = buttonsAvatarSelectedAdapter
    }

    override fun onGlobalLayout() {
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        if (view != null) {
            Log.d("TEST_GAME", "AvatarPickerFragment -> this.recyclerButtons height: ${this.recyclerButtons!!.measuredHeight}")

            val refreshImageProfile = avatarViewModel.refreshImageProfile(this)
                    .subscribeOn(Schedulers.io())

            val movePage = avatarViewModel.movePage(this)
                    .subscribeOn(Schedulers.io())

            compositeDisposableOnDestroyed.addAll(
                    Observable.combineLatest(refreshImageProfile.startWith(0f), movePage.startWith(0f),
                            BiFunction { refreshImageAlpha: Float, movePageAlpha: Float -> 0f })
                            .subscribeOn(Schedulers.io())
                            .filter { this.avatarImageTmp.alpha != it }
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext { Log.d("TEST_GAME", "1 AvatarPickerFragment -> animateAvatarImageTmp: $it") }
                            .doOnNext(this::animateVisibilityAvatarImageTmp)
                            .flatMap { Observable.fromCallable { actionOkButton.measuredHeight.toFloat() } }
                            .doOnNext(this::animateActionOkButton)
                            .subscribe()

//                    RxView.globalLayouts(this.avatarImageTmp)
//                            .firstOrError()
//                            .subscribe { t1, t2 ->
//                                val layoutParams = this.avatarImageTmp.layoutParams
//                                val ratio = resources.displayMetrics.widthPixels * 0.35
//                                layoutParams.width = ratio.toInt()
//                                layoutParams.height = ratio.toInt()
//                                this.avatarImageTmp.layoutParams = layoutParams
//                                this.avatarImageTmp.requestLayout()
//                            }
            )

            this.avatarViewModel.visibleMainScreen(this.avatarImageTmp, viewPager, this.recyclerButtons)


            compositeDisposableOnDestroyed.addAll(

                    this.avatarViewModel.isCloseScreen(this)
                            .filter(Functions.equalsWith(true))
                            .subscribe { dismissAllowingStateLoss() },

                    this.avatarViewModel
                            .getPagerManager(
                                    viewPager
                                    , this.activity!!
                                    , this.image_profile_hp.bottom + this.recyclerButtons!!.measuredHeight
                                    , this.childFragmentManager
                            )
//                            .observeOn(Schedulers.io())
//                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnEvent { viewPagerManager, t2 -> addListener(viewPagerManager) }
                            .map(ViewPagerManager::getInfoScrollPageData)
                            .flatMap(this.avatarViewModel::getButtonsAvatarSelectedAdapter)
                            .doOnEvent { buttonsAvatarSelectedAdapter, t2 -> addListener(buttonsAvatarSelectedAdapter) }
                            .subscribe(this::initRecyclerViewButtonsAvatarSelected),

                    avatarViewModel
                            .saveImage(activity!!)
                            .subscribeOn(Schedulers.io())
                            .map { actionOkButton.measuredHeight.toFloat() }
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(this::animateActionOkButton)
                            .flatMapCompletable {
                                animateImageProfile()
                                        .observeOn(Schedulers.io())
                                        .doOnEvent { avatarViewModel.finishAnimateImageProfile() }
                            }
                            ?.subscribe()!!,

                    avatarViewModel
                            .getImageProfileTmp(this)
                            .subscribeOn(Schedulers.io())
                            .subscribe(avatarImageTmp::setImageBitmap),

                    avatarViewModel
                            .imageProfileTmpChange(this)
                            .subscribeOn(Schedulers.io())
                            .map { 1f }
                            .filter { this.avatarImageTmp.alpha != it }
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext { Log.d("TEST_GAME", "2 AvatarPickerFragment -> animateAvatarImageTmp") }
                            .doOnNext(this::animateVisibilityAvatarImageTmp)
                            .map { 0f }
                            .doOnNext(this::animateActionOkButton)
                            .subscribe(),

                    RxView.clicks(actionOkButton)
                            .firstOrError()
                            .doOnEvent { t1, t2 -> avatarViewModel.actionOkClick() }
                            .subscribe()
            )

        }
    }

}