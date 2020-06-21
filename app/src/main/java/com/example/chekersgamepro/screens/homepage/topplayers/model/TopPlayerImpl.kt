package com.example.chekersgamepro.screens.homepage.topplayers.model

class TopPlayerImpl(
        private val playerName: String = "",
        private val totalWin: Int = -1,
        private val totalLoss: Int = -1,
        private val money: Int = -1,
        private val avatarEncode: String = "") : ITopPlayer {

    var isNumber : Boolean  = false

    var position : Int = -1

    override fun getPlayerName() = this.playerName

    override fun getTotalWin() = this.totalWin

    override fun getTotalLoss() = this.totalLoss

    override fun getMoney() = this.money

    override fun getAvatarEncode() = this.avatarEncode

    override fun getPlayerPositionSign() = if (this.isNumber) this.position.toString() else "-"

}