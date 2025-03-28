package kr.apo2073.chunkly.utils

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.LangManager.translate
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*

object EconManager {
    private val plugin = Chunkly.plugin
    private val econ = Chunkly.econ

    private fun giveMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            econ.depositPlayer(offlinePlayer, amount)
        })
    }

    private fun takeMoney(offlinePlayer: OfflinePlayer, amount: Double) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            econ.withdrawPlayer(offlinePlayer, amount)
        })
    }

    private fun getMoney(offlinePlayer: OfflinePlayer, callback: (Double) -> Unit) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val balance = econ.getBalance(offlinePlayer)
            callback(balance)
        })
    }

    fun buyChunk(chunk: Chunk, owner: Player) {
        var price = plugin.config.getDouble("chunk-price.each.${chunk.world.name}", 0.0)
        if (price == 0.0 || price == null) price = plugin.config.getDouble("chunk-price.default")

        getMoney(owner) { balance ->
            if (balance < price) {
                owner.sendMessage(translate("chunk.not.enough.money"), true)
                return@getMoney
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                val chunks = Chunks(chunk)
                chunks.setOwner(owner)
                UserData.addChunk(chunk.chunkKey.toString(), owner.uniqueId)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    takeMoney(owner, price)
                    owner.inventory.itemInMainHand.amount -= 1
                    owner.sendMessage(translate("chunk.buy.suc"), true)
                })
            })
        }
    }

    fun sellChunk(chunk: Chunk, to: Player?) {
        var price = plugin.config.getDouble("chunk-price.each.${chunk.world.name}", 0.0)
        if (price == 0.0 || price == null) price = plugin.config.getDouble("chunk-price.default")

        val chunks = Chunks(chunk)
        val ownerUUID = chunk.persistentDataContainer.get(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING
        )?.let { UUID.fromString(it) } ?: return
        val toPlayer = to ?: return
        val owner = Bukkit.getPlayer(ownerUUID) ?: return

        getMoney(toPlayer) { balance ->
            if (balance < price) {
                toPlayer.sendMessage(translate("chunk.not.enough.money"), true)
                return@getMoney
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                UserData.removeChunk(chunk.chunkKey.toString(), ownerUUID)
                UserData.addChunk(chunk.chunkKey.toString(), toPlayer.uniqueId)
                chunks.setOwner(toPlayer)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    takeMoney(toPlayer, price)
                    giveMoney(owner, price)
                    toPlayer.sendMessage(translate("chunk.own.bought"), true)
                    owner.sendMessage(translate("chunk.own.to.other").replace("{to}", toPlayer.name), true)
                })
            })
        }
    }
}