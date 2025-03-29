package kr.apo2073.chunkly.events

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.chunks.RegionsInChunks
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
import java.io.File
import java.util.*

class PlayerInteraction : Listener {
    private val plugin = Chunkly.plugin

    @EventHandler
    fun PlayerInteractEvent.onInteraction() {
        if (!player.hasPermission("apo.chunkly.buy")) return
        val itemInHand = this.item ?: return

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val file = getConfigFile("items")
            val config = YamlConfiguration.loadConfiguration(file)
            val chunkItem = config.getItemStack("items") ?: return@Runnable

            if (!itemInHand.isSimilar(chunkItem)) return@Runnable

            val chunk = player.chunk
            val chunks = Chunks(chunk)

            fun proceedWithPurchase() {
                UserData.countChunkInWorld(player.world.uid, player.uniqueId) { count ->
                    val maxCount = plugin.config.getInt(
                        "bought-limit.${player.world.name}",
                        plugin.config.getInt("bought-limit.default")
                    )
                    if (count + 2 > maxCount) {
                        player.sendMessage(translate("chunk.cant.buy.more"), true)
                        return@countChunkInWorld
                    }
                    EconManager.buyChunk(chunk, player)
                }
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                isCancelled = true
                if (!chunks.canBuy()) {
                    if (chunks.getOwner() == player.name) {
                        player.sendMessage(translate("chunk.its.yours"), true)
                    } else {
                        player.sendMessage(translate("command.ground.cant.buy"), true)
                    }
                    return@Runnable
                }

                if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                    RegionsInChunks(plugin).getRegionsInChunkAsync(chunk) { regions ->
                        val block = plugin.config.getStringList("cant-bought-region")
                        for (region in regions) {
                            if (region.id in block) {
                                player.sendMessage(translate("chunk.cant.buy.region"), true)
                                return@getRegionsInChunkAsync
                            }
                        }
                        proceedWithPurchase()
                    }
                } else {
                    proceedWithPurchase()
                }
            })
        })
    }

    @EventHandler
    fun ChunkLoadEvent.onLoad() {
        val chunks = Chunks(chunk)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            chunks.getMembers() {
                val members=it?.mapNotNull { Bukkit.getPlayer(it) } ?: return@getMembers
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    members.forEach {
                        it.addAttachment(plugin)
                            .setPermission(
                                "apo.chunkly.${chunks.getChunk()?.chunkKey ?: return@Runnable}",
                                true
                            )
                    }
                })
            }
        })
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        val uuid = player.uniqueId
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val file = File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
            val config = YamlConfiguration.loadConfiguration(file)
            config.set("user.name", player.name)
            config.set("user.uuid", uuid.toString())
            config.save(file)
        })
    }

    private var playerChunk = mutableMapOf<UUID, Chunk?>()

    @EventHandler
    fun PlayerMoveEvent.onMove() {
        var chunk = playerChunk[player.uniqueId]
        if (chunk != player.chunk) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val event = PlayerChunkChangeEvent(
                    chunk,
                    player.chunk,
                    player
                )
                Bukkit.getPluginManager().callEvent(event)
            })
            chunk = player.chunk
            playerChunk[player.uniqueId] = chunk
        }
    }
}