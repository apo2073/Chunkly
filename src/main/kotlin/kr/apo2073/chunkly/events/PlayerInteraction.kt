package kr.apo2073.chunkly.events

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.ChunkBorder
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.sendMessage
import kr.apo2073.chunkly.utils.str2Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack

class PlayerInteraction:Listener {
    private val chunkBounder=ChunkBorder()
    init {
//        this.chunkBounder.startParticleTask()
    }
    private val plugin=Chunkly.plugin
    @EventHandler
    fun PlayerInteractEvent.onInteraction() {
        val file=getConfigFile("items")
        val config=YamlConfiguration.loadConfiguration(file)
        val chunkItem= config.getItemStack("items") ?: return

        if (!(this.item?.isSimilar(chunkItem) ?: return)) return
        val chunk=player.chunk
//        chunk.also { Bukkit.broadcast("${it.x}, ${it.z}".str2Component()) }
        val chunks=Chunks(chunk)
        if (!chunks.canBuy()) {
            if (chunks.getOwner()==player.name) {
                player.sendMessage(translate("chunk.its.yours"), true)
                return
            }
            player.sendMessage(translate("command.ground.cant.buy"), true)
            return
        }
        EconManager.buyChunk(chunk, player)
    }

//    @EventHandler
//    fun PlayerItemHeldEvent.onHeld() {
//        val file=getConfigFile("items")
//        val config=YamlConfiguration.loadConfiguration(file)
//        val chunkItem= config.getItemStack("items") ?: return
//
////        chunkBounder.enabledPlayers.remove(player.uniqueId)
//        if (player.inventory.itemInMainHand.isEmpty) return
//        if (!this.player.inventory.itemInMainHand.isSimilar(chunkItem)) return
////        chunkBounder.enabledPlayers.add(player.uniqueId)
//    }

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
}