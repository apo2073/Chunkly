package kr.apo2073.chunkly.utils

import kr.apo2073.chunkly.Chunkly
import org.bukkit.OfflinePlayer

object EconManager {
    private val plugin=Chunkly.plugin
    private val econ=Chunkly.econ

    fun giveMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        econ.depositPlayer(offlinePlayer, amount)
    }

    fun takeMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        econ.withdrawPlayer(offlinePlayer, amount)
    }

    fun getMoney(offlinePlayer: OfflinePlayer): Double {
        return econ.getBalance(offlinePlayer)
    }
}