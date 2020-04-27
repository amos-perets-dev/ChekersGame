package com.example.chekersgamepro.screens.homepage.avatar.model.pages

import androidx.fragment.app.Fragment
import com.example.chekersgamepro.screens.homepage.avatar.fragemnts.AvatarFragmentBase

interface IPagesManager {

    fun getFragments() : List<Fragment>

    fun setNewPostion(newPosition : Int)

    fun isActivePage(page: AvatarFragmentBase) : Boolean
}