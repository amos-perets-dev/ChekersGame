package com.example.chekersgamepro.screens.homepage.avatar

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.adapters.AvatarPagerAdapter
import com.example.chekersgamepro.screens.homepage.avatar.model.data.ScrollPageData
import com.example.chekersgamepro.util.DisplayUtil
import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.support.v4.view.RxViewPager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class ViewPagerManager(private val pager: ViewPager
                       , activity: FragmentActivity?
                       , bottom: Int
                       , childFragmentManager: FragmentManager
                       , fragmentsList: List<Fragment>
                       , changeAvatarScreen: Observable<Int>) : CheckersFragment.LifecycleListener {

    private val infoScrollPage = PublishSubject.create<ScrollPageData>()

    private val compositeDisposable = CompositeDisposable()

    private val pagerAdapter = AvatarPagerAdapter(childFragmentManager, fragmentsList)

    init {

        val displayMatrix = DisplayUtil.getDisplayMatrix(activity!!)
        val height = displayMatrix.heightPixels
        val width = displayMatrix.widthPixels

        val layoutParams = pager.layoutParams

        val mPagerHeight = height - bottom - activity.resources.getDimensionPixelSize(R.dimen.avatar_fragment_top_margin)
        layoutParams.height = mPagerHeight
        layoutParams.width = width
        pager.layoutParams = layoutParams

        // The pager adapter, which provides the pages to the view pager widget.

        val pagerAdapter = AvatarPagerAdapter(childFragmentManager, fragmentsList)

        var oldPosition = 0
        var isRightDirection: Boolean
        compositeDisposable.add( RxViewPager.pageSelections(pager)
                .skipInitialValue()
                .subscribeOn(Schedulers.io())
                .map {newPosition ->
                    Log.d("TEST_GAME", "onPageSelected: $newPosition")
                    val fragmentToShow = pagerAdapter.getItem(newPosition) as CheckersFragment.FragmentLifecycle
                    fragmentToShow.onResumeFragment(newPosition)
                    val fragmentToHide = pagerAdapter.getItem(oldPosition) as CheckersFragment.FragmentLifecycle
                    fragmentToHide.onPauseFragment()

                    isRightDirection = newPosition > oldPosition

                    ScrollPageData(isRightDirection, oldPosition, newPosition)
                }
                .doOnNext(infoScrollPage::onNext)
                .subscribe {infoScrollPage ->
                    oldPosition = infoScrollPage.newPosition
                }
        )

        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = fragmentsList.size
        pager.setPageTransformer(true, ZoomOutPageTransformer())

        compositeDisposable.add(changeAvatarScreen
                .distinctUntilChanged()
                .subscribe(this::scrollToPosition)
        )

    }

    fun getPagerAdapter() = pagerAdapter

    private fun scrollToPosition(position : Int){
        pager.setCurrentItem(position, true)
    }

    fun getInfoScrollPageData(): Observable<ScrollPageData> = infoScrollPage.hide()

    fun dispose(){
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        dispose()
    }


}