package com.example.chekersgamepro.screens.homepage.avatar.model.pages

import androidx.fragment.app.Fragment
import com.example.chekersgamepro.screens.homepage.avatar.AvatarViewModel
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarCameraFragment
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarDefaultFragment
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarFragmentBase
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarGalleryFragment

class PagesManagerImpl(avatarViewModel: AvatarViewModel) : IPagesManager {

    private val avatarCameraFragment = AvatarCameraFragment.newInstance(avatarViewModel)
    private val avatarDefaultFragment = AvatarDefaultFragment.newInstance(avatarViewModel)
    private val avatarGalleryFragment = AvatarGalleryFragment.newInstance(avatarViewModel)

    private val fragmentList = ArrayList<Fragment>()

    private var indexActiveFragment = 0

    init { addFragments() }

    private fun addFragments() {
        fragmentList.add(avatarCameraFragment)
        fragmentList.add(avatarDefaultFragment)
        fragmentList.add(avatarGalleryFragment)
    }

    override fun getFragments(): List<Fragment> = this.fragmentList

    override fun setNewPostion(newPosition: Int) {
        this.indexActiveFragment = newPosition
    }

    override fun isActivePage(page: AvatarFragmentBase): Boolean {
        val activePage = fragmentList[indexActiveFragment]
        return page == activePage
    }

}