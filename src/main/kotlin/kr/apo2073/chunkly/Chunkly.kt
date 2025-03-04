package kr.apo2073.chunkly

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import kr.apo2073.chunkly.cmds.GroundCommand
import kr.apo2073.chunkly.events.PlayerInteraction
import kr.apo2073.chunkly.utils.LangManager.translate
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin


class Chunkly : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
            private set
        lateinit var protocolManager:ProtocolManager
            private set
        lateinit var econ:Economy
    }

    override fun onEnable() {
        if (!setupEconomy() ) {
            logger.severe(translate("plugin.disable.cause.vault"));
            server.pluginManager.disablePlugin(this);
            return;
        }

        plugin = this
        protocolManager= ProtocolLibrary.getProtocolManager()

        saveDefaultConfig()
        saveResource("lang/ko.json", true)

        server.pluginManager.registerEvents(PlayerInteraction(), this)
        GroundCommand(this)
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.provider
        return econ != null
    }
}
