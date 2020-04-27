package com.example.chekersgamepro.screens.homepage.avatar.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class AvatarPagerAdapter (fm: FragmentManager, private val fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int = this.fragments.size

    override fun getItem(position: Int): Fragment = this.fragments[position]
}