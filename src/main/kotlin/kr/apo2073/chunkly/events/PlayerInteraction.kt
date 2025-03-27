package kr.apo2073.chunkly.events

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.events.onChunk.PlayerChunkChangeEvent
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.sendMessage
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.ChunkLoadEvent
import java.util.*

class PlayerInteraction:Listener {
    private val plugin=Chunkly.plugin
    @EventHandler
    fun PlayerInteractEvent.onInteraction() {
        val file=getConfigFile("items")
        val config=YamlConfiguration.loadConfiguration(file)
        val chunkItem= config.getItemStack("items") ?: return

        if (!player.hasPermission("apo.chunkly.buy")) return
        if (!(this.item?.isSimilar(chunkItem) ?: return)) return
        val chunk=player.chunk
        val chunks=Chunks(chunk)
        isCancelled=true
        if (!chunks.canBuy()) {
            if (chunks.getOwner()==player.name) {
                player.sendMessage(translate("chunk.its.yours"), true)
                return
            }
            player.sendMessage(translate("command.ground.cant.buy"), true)
            return
        }
        val playerHasChunk=UserData.countChunkInWorld(player.world.uid, player.uniqueId)
        val maxCount=plugin.config.getInt(
            "bought-limit.${player.world.name}",
            plugin.config.getInt("bought-limit.default")
        )
        if (playerHasChunk+2>maxCount) {
            player.sendMessage(translate("chunk.cant.buy.more"), true)
            return
        }
        EconManager.buyChunk(chunk, player)
    }

    @EventHandler
    fun ChunkLoadEvent.onLoad() {
        val chunks= Chunks(chunk)
        val members=chunks.getMembers()?.map { Bukkit.getPlayer(it) ?: return } ?: return
        members.forEach {
            it.addAttachment(plugin)
                .setPermission(
                    "apo.chunkly.${chunks.getChunk()?.chunkKey ?: return}",
                    true
                )
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        val uuid=player.uniqueId

        UserData.setValue("user.name", player.name, uuid)
        UserData.setValue("user.uuid", uuid.toString(), uuid)
    }

    private var playerChunk= mutableMapOf<UUID, Chunk?>()
    @EventHandler
    fun PlayerMoveEvent.onMove() {
        var chunk=playerChunk[player.uniqueId]
        if (chunk!=player.chunk) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val event=PlayerChunkChangeEvent(
                    chunk,
                    player.chunk,
                    player
                )
                Bukkit.getPluginManager().callEvent(event)
            })
            chunk=player.chunk
            playerChunk[player.uniqueId]=chunk
        }
    }
}