package kr.apo2073.chunkly

import kr.apo2073.chunkly.chunks.ChunkBorder
import kr.apo2073.chunkly.cmds.ChunkCommand
import kr.apo2073.chunkly.events.PlayerInteraction
import kr.apo2073.chunkly.papi.PlaceHolderHandler
import kr.apo2073.chunkly.utils.LangManager.translate
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Chunkly : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
            private set
//        lateinit var protocolManager:ProtocolManager
//            private set
        lateinit var econ:Economy
    }

    override fun onEnable() {
        plugin = this
        try{
            saveDefaultConfig()
            saveResource("lang/ko.json", true)
            saveResource("chunkdata/example-chunk.yml", true)
            saveResource("userdata/example-user.yml", true)

            if (!setupEconomy() ) {
                logger.severe(translate("plugin.disable.cause.vault"));
                server.pluginManager.disablePlugin(this);
                return;
            }

//            protocolManager = ProtocolLibrary.getProtocolManager()

            server.pluginManager.registerEvents(PlayerInteraction(), this)
            ChunkCommand(this)
            ChunkBorder.start()

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                PlaceHolderHandler().register()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(
            Economy::class.java
        ) ?: return false
        econ = rsp.provider
        return econ != null
    }
}
