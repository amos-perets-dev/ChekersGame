package com.example.chekersgamepro.screens.homepage.topplayers.model

import android.graphics.Bitmap
import io.reactivex.Observable

class TopPlayerImpl(
        private val playerName: String = "",
        private val totalWin: Int = -1,
        private val totalLoss: Int = -1,
        private val money: Int = -1,
        private val avatar: Observable<Bitmap>,
        private val isNumber: Boolean = false,
        private val position: Int = -1) : ITopPlayer {


    override fun getPlayerName() = this.playerName

    override fun getTotalWin() = this.totalWin

    override fun getTotalLoss() = this.totalLoss

    override fun getMoney() = this.money

    override fun getAvatar() = this.avatar

    override fun getPlayerPositionSign() = if (this.isNumber) this.position.toString() else "-"

}