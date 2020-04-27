package com.example.chekersgamepro.screens.homepage.avatar.fragemnts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.chekersgamepro.checkers.CheckersFragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.ViewPagerManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

open class AvatarFragmentBase(private val avatarViewModel: AvatarViewModel) : CheckersFragment(), CheckersFragment.FragmentLifecycle {

    companion object {
        fun newInstance(avatarViewModel: AvatarViewModel): AvatarFragmentBase {
            return AvatarFragmentBase(avatarViewModel)
        }
    }

    protected open fun isMovePage(): Observable<Boolean> =  avatarViewModel.movePage(activity!!).map { true }

    override fun onPauseFragment() {
        Log.d("TEST_GAME", "AvatarFragmentBase -> onPauseFragment")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        closeFragmentBySwipe(view)

    }

    override fun onResumeFragment(newPosition: Int) {
        avatarViewModel.notifyMovePage(newPosition)
        Log.d("TEST_GAME", "AvatarFragmentBase -> onResumeFragment")
    }

}