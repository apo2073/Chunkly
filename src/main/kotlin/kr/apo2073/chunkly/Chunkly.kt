package kr.apo2073.chunkly

import com.comphenix.protocol.ProtocolLibrary
import kr.apo2073.chunkly.cmds.GroundCommand
import kr.apo2073.chunkly.events.PlayerInteraction
import com.comphenix.protocol.ProtocolManager
import kr.apo2073.chunkly.chunks.ChunkBorder
import org.bukkit.plugin.java.JavaPlugin

class Chunkly : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
            private set
        lateinit var protocolManager:ProtocolManager
            private set
    }

    override fun onEnable() {
        plugin = this
        protocolManager= ProtocolLibrary.getProtocolManager()

        saveDefaultConfig()
        saveResource("lang/ko.json", true)

        server.pluginManager.registerEvents(PlayerInteraction(), this)
        GroundCommand(this)
    }
}
