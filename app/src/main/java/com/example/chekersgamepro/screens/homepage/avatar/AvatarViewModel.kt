package com.example.chekersgamepro.screens.homepage.avatar

import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.avatar.adapters.ButtonsAvatarSelectedAdapter
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarFragmentBase
import com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons.ButtonsAvatarImpl
import com.example.chekersgamepro.screens.homepage.avatar.model.button.buttons.IButtonsAvatar
import com.example.chekersgamepro.screens.homepage.avatar.model.pages.IPagesManager
import com.example.chekersgamepro.screens.homepage.avatar.model.pages.PagesManagerImpl
import com.example.chekersgamepro.screens.homepage.avatar.model.data.AvatarData
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import com.example.chekersgamepro.util.PermissionUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_avatar.view.*
import java.util.concurrent.TimeUnit

class AvatarViewModel : ViewModel() {

    private val avatarData = MutableLiveData<AvatarData>()

    private val changeAvatarScreen = MutableLiveData<Int>()

    private val imageProfile = MutableLiveData<Bitmap>()

    private val compositeDisposable = CompositeDisposable()

    private val repositoryManager = RepositoryManager.create()

    private val pagesManager: IPagesManager = PagesManagerImpl(this)

    private val buttonsAvatar: IButtonsAvatar = ButtonsAvatarImpl()

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

    fun setChangeAvatarData(avatarData: AvatarData) {
        if (avatarData.image != null) {
            imageProfile.postValue(avatarData.image!!)
        }
        setAvatarData(avatarData)
    }

    private fun getFragments() = pagesManager.getFragments()

//    fun saveImageProfile()/*: Completable*/ {
//        Log.d("TEST_GAME", "1 saveImageProfile")
//        Log.d("TEST_GAME", "2 saveImageProfile")
////        saveAvatar()
//        /*return*/ this.repositoryManager.storeImage()
////                .subscribeOn(Schedulers.io())
//    }

    fun actionOkClick() {
//        this.repositoryManager.storeImage()
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
//                    .subscribeOn(Schedulers.io())
                    .map { it.avatarState.ordinal == AvatarState.AVATAR_SAVE.ordinal }
                    .startWith(false)
                    .filter(Functions.equalsWith(true))
                    .distinctUntilChanged()

    fun getImageProfileTmp(lifecycleOwner: LifecycleOwner): Observable<Bitmap> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, imageProfile))


    fun isNeedAnimateGalleryButton(page: AvatarFragmentBase): Observable<Boolean> =
            getImageProfileTmp(page)
                    .subscribeOn(Schedulers.io())
                    .flatMap { Observable.fromCallable { isActivePage(page) } }
                    .delay(450, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter(Functions.equalsWith(true))

    fun getImageProfileTmpChange(page: AvatarFragmentBase): Observable<Bitmap> =
            getImageProfileTmp(page)
                    .subscribeOn(Schedulers.io())
                    .flatMap { image ->
                        Observable.fromCallable { isActivePage(page) }
                                .filter(Functions.equalsWith(true))
                                .map { image }
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
            buttonsAvatar.createButtonAvatarSelectedList(infoScrollPageData).subscribeOn(Schedulers.io()).map { ButtonsAvatarSelectedAdapter(it) }

    override fun onCleared() {
        Log.d("TEST_GAME", "AvatarViewModel -> onCleared")

        compositeDisposable.clear()
        super.onCleared()

    }

    fun finishAnimateImageProfile() {
        compositeDisposable.add(
                this.repositoryManager
                        .storeImage()
                        .subscribe()
        )
    }

    fun getPagerManager(avatarPager: ViewPager
                        , activity: FragmentActivity
                        , bottom: Int
                        , childFragmentManager: FragmentManager): Single<ViewPagerManager> =
            Single.create {
                it.onSuccess(ViewPagerManager(
                        avatarPager, activity, bottom, childFragmentManager, getFragments(), changeAvatarScreen(activity)))
            }

}