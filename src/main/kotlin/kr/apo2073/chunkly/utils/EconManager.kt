package kr.apo2073.chunkly.utils

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.utils.LangManager.translate
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object EconManager {
    private val plugin=Chunkly.plugin
    private val econ=Chunkly.econ

    private fun giveMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        econ.depositPlayer(offlinePlayer, amount)
    }

    private fun takeMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        econ.withdrawPlayer(offlinePlayer, amount)
    }

    private fun getMoney(offlinePlayer: OfflinePlayer): Double {
        return econ.getBalance(offlinePlayer)
    }

    fun buyChunk(chunk: Chunk, owner: Player) {
        var price= plugin.config.getDouble("chunk-price.each.${chunk.world.name}")
        if (price==0.0 || price==null) price= plugin.config.getDouble("chunk-price.default")
        if (getMoney(owner)>=price) {
            owner.sendMessage(translate("chunk.buy.suc"), true)
        } else {
            owner.sendMessage(translate("chunk.not.enough.money"), true)
            return
        }
        val chunks=Chunks(chunk)
        chunks.setOwner(owner)
        takeMoney(owner, price)
    }

    fun sellChunk(chunk: Chunk, to:Player) {
        var price= plugin.config.getDouble("chunk-price.each.${chunk.world.name}")
        if (price==0.0 || price==null) price= plugin.config.getDouble("chunk-price.default")
        val chunks=Chunks(chunk)
        chunks.setOwner(to)
        val owner= Bukkit.getPlayer(chunks.getOwner() ?: return) ?: return
        takeMoney(to, price)
        giveMoney(owner, price)
        to.sendMessage(translate("chunk.own.bought"), true)
        owner.sendMessage(translate("chunk.own.to.other").replace("{to}", to.name), true)
    }
}