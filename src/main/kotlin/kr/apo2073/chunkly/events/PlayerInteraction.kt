package kr.apo2073.chunkly.events

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.ChunkBorder
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.str2Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent

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
        chunk.also {
            Bukkit.broadcast("${it.x}, ${it.z}".str2Component())
        }
    }
    @EventHandler
    fun PlayerItemHeldEvent.onHeld() {
        val file=getConfigFile("items")
        val config=YamlConfiguration.loadConfiguration(file)
        val chunkItem= config.getItemStack("items") ?: return

//        chunkBounder.enabledPlayers.remove(player.uniqueId)
        if (player.inventory.itemInMainHand.isEmpty) return
        if (!this.player.inventory.itemInMainHand.isSimilar(chunkItem)) return
//        chunkBounder.enabledPlayers.add(player.uniqueId)
    }
}