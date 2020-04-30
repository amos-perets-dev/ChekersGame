package com.example.chekersgamepro.screens.homepage.avatar.model.defaultt.avatar

import com.example.chekersgamepro.screens.homepage.avatar.adapters.DefaultAvatarAdapter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface IDefaultAvatars  {

    fun getAvatarsDefaultAdapter() : Single<DefaultAvatarAdapter>?
}