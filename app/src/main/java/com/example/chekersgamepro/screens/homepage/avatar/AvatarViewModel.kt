package com.example.chekersgamepro.screens.homepage.avatar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.avatar.adapters.ButtonsAvatarSelectedAdapter
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarFragmentBase
import com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons.ButtonsAvatarImpl
import com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons.IButtonsAvatar
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar.IDefaultAvatars
import com.example.chekersgamepro.screens.homepage.avatar.model.pages.IPagesManager
import com.example.chekersgamepro.screens.homepage.avatar.model.pages.PagesManagerImpl
import com.example.chekersgamepro.util.IntentUtil
import com.example.chekersgamepro.util.PermissionUtil
import com.example.chekersgamepro.views.custom.CustomViewPager
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AvatarViewModel : ViewModel() {

    private val avatarData = MutableLiveData<AvatarData>()

    private val changeAvatarScreen = MutableLiveData<Int>()

    private val imageProfile = MutableLiveData<Bitmap>()

    private val closeScreen = MutableLiveData<Boolean>()

    private val isOpenGallery = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    private val repositoryManager = RepositoryManager.create()

    private lateinit var pagesManager: IPagesManager

    private val buttonsAvatar: IButtonsAvatar = ButtonsAvatarImpl()

    private val imageUtil = CheckersImageUtil.create()

    private val context = CheckersApplication.create().applicationContext

    private lateinit var defaultAvatars: IDefaultAvatars

    init {
        compositeDisposable.add(
                buttonsAvatar
                        .getButtonClick()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { Log.d("TEST_GAME", "AvatarViewModel getButtonClick") }
                        .subscribe(changeAvatarScreen::postValue)
        )
    }


    fun visibleMainScreen(avatarImageTmp: CircleImageView, viewPager: CustomViewPager, recyclerButtons: RecyclerView) {
        pagesManager = PagesManagerImpl(this, avatarImageTmp, viewPager,recyclerButtons)
    }


    fun setChangeAvatarData(avatarData: AvatarData) {
        if (avatarData.image != null) {
            imageProfile.postValue(avatarData.image!!)
        }
        setAvatarData(avatarData)
    }

    private fun getFragments() = pagesManager.getFragments()

    fun actionOkClick() {
        Log.d("TEST_GAME", "AvatarViewModel saveAvatar")
        setAvatarData(AvatarData(AvatarState.AVATAR_SAVE, null))
    }

    private fun changeAvatarScreen(lifecycleOwner: LifecycleOwner): Observable<Int> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, changeAvatarScreen))
                    .distinctUntilChanged()

    fun imageProfileTmpChange(lifecycleOwner: LifecycleOwner): Observable<Bitmap> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, avatarData))
                    .filter { it.avatarState.ordinal == AvatarState.CAPTURE_AVATAR.ordinal }
                    .doOnNext { Log.d("TEST_GAME", "AvatarViewModel imageProfileTmpChange ${it.image}") }
                    .doOnNext { this.repositoryManager.setImageProfileTmp(it.image) }
                    .map(AvatarData::image)

    fun refreshImageProfile(lifecycleOwner: LifecycleOwner): Observable<Float> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, avatarData))
                    .map { it.avatarState.ordinal == AvatarState.REFRESH_AVATAR_CAMERA.ordinal }
                    .filter(Functions.equalsWith(true))
                    .map { 0f }

    fun movePage(lifecycleOwner: LifecycleOwner): Observable<Float> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, avatarData))
                    .map { it.avatarState.ordinal == AvatarState.AVATAR_MOVE_PAGE.ordinal }
                    .filter(Functions.equalsWith(true))
                    .map { 0f }

    fun saveImage(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, avatarData))
                    .subscribeOn(Schedulers.io())
                    .map { it.avatarState.ordinal == AvatarState.AVATAR_SAVE.ordinal }
                    .startWith(false)
                    .filter(Functions.equalsWith(true))
                    .distinctUntilChanged()

    fun getImageProfileTmp(lifecycleOwner: LifecycleOwner): Observable<Bitmap> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, imageProfile))

    fun isCloseScreen(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, closeScreen))
                    .startWith(false)

    fun getOpenGalleryText(lifecycleOwner: LifecycleOwner): Observable<String> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, isOpenGallery))
                    .map { isPermissionsGranted ->
                        context.getString(
                                if (isPermissionsGranted) {
                                    R.string.activity_home_page_avatar_add_gallery_photo
                                } else {
                                    R.string.activity_home_page_avatar_add_gallery_photo_permission
                                })

                    }
                    .startWith(context.getString(
                            if (PermissionUtil.isAllPermissionsGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                R.string.activity_home_page_avatar_add_gallery_photo
                            } else {
                                R.string.activity_home_page_avatar_add_gallery_photo_permission
                            })
                    )

    fun openGallery(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, isOpenGallery))
                    .filter(Functions.equalsWith(true))
                    .map { IntentUtil.createOpenGalleryIntent() }

    fun isNeedAnimateGalleryButton(page: AvatarFragmentBase): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(page, avatarData))
                    .subscribeOn(Schedulers.io())
                    .map { it.avatarState.ordinal == AvatarState.AVATAR_FROM_GALLERY.ordinal }
                    .delay(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter(Functions.equalsWith(true))

    fun getImageProfileTmpChange(page: AvatarFragmentBase): Observable<Drawable> =
            getImageProfileTmp(page)
                    .subscribeOn(Schedulers.io())
                    .flatMap { image ->
                        Observable.fromCallable { isActivePage(page) }
                                .filter(Functions.equalsWith(true))
                                .map { image }
                                .map { imageUtil.blurBitmapFromBitmap(it) }
                                .map { imageUtil.bitmapToDrawable(it) }

                    }

    fun notifyMovePage(newPosition: Int) {
        pagesManager.setNewPostion(newPosition)
        setAvatarData(AvatarData(AvatarState.AVATAR_MOVE_PAGE, null))
    }

    private fun isActivePage(page: AvatarFragmentBase): Boolean {
        return pagesManager.isActivePage(page)
    }

    private fun setAvatarData(avatarData: AvatarData) {
        this.avatarData.postValue(avatarData)
    }

    fun getDefaultAvatarsList(): Single<List<Bitmap>> {
        return CheckersConfiguration.getInstance().getDefaultAvatarsList()!!
    }

    fun getButtonsAvatarSelectedAdapter(infoScrollPageData: Observable<ScrollPageData>) =
            buttonsAvatar.createButtonAvatarSelectedList(infoScrollPageData)
                    .subscribeOn(Schedulers.io())
                    .map { ButtonsAvatarSelectedAdapter(it) }

    override fun onCleared() {
        Log.d("TEST_GAME", "AvatarViewModel -> onCleared")

        compositeDisposable.clear()
        super.onCleared()

    }

    fun finishAnimateImageProfile() {
        notifyCloseScreen()
        compositeDisposable.add(
                this.repositoryManager
                        .storeImage()
                        .subscribe()
        )
    }

    private fun notifyCloseScreen() {
        closeScreen.postValue(true)

    }

    fun getPagerManager(avatarPager: ViewPager
                        , activity: FragmentActivity
                        , bottom: Int
                        , childFragmentManager: FragmentManager): Single<ViewPagerManager> =
            Single.create {
                it.onSuccess(ViewPagerManager(
                        avatarPager, activity, bottom, childFragmentManager, getFragments(), changeAvatarScreen(activity)))
            }

    fun setDataFromGallery(data: Intent?) {
        setChangeAvatarData(AvatarData(AvatarState.AVATAR_FROM_GALLERY, null))
        compositeDisposable.add(
                Observable.fromCallable { data }
                        .subscribeOn(Schedulers.io())
                        .map { dataImage -> imageUtil.creteBitmapFromData(dataImage, true) }
                        .map { AvatarData(AvatarState.CAPTURE_AVATAR, it) }
                        .subscribe(this::setChangeAvatarData)
        )
    }

    fun permissionDenied() {
        notifyCloseScreen()
    }

    fun clickOnGallery(context: Context) {

        compositeDisposable.add(
                PermissionUtil.isStoragePermissionGranted(context)
                .subscribe { isOpenGallery.postValue(it) }
        )
    }

    fun defaultAvatarsClick(): Boolean {

        return true
    }


}