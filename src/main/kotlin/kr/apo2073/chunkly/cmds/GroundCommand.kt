package kr.apo2073.chunkly.cmds

import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.sendMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class GroundCommand(plugin: JavaPlugin): TabExecutor {
    init {
        plugin.getCommand("땅")?.apply {
            setExecutor(this@GroundCommand)
            tabCompleter=this@GroundCommand
        }
    }
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return true
        if (p3.isEmpty()) {
            sendUsage(p0)
            return true
        }
        if (p3.size==1) {
            when(p3[0]) {
                "구매아이템설정"-> performItem(p0)
                else-> sendUsage(p0)
            }

            return true
        }
        return true
    }

    private fun performItem(p0: CommandSender) {
        val player=p0 as Player
        if (player.inventory.itemInMainHand.isEmpty) {
            player.sendMessage("손에 아이템을 들어주세요!", true)
            return
        }
        val file=getConfigFile("items")
        val config=YamlConfiguration.loadConfiguration(file)
        config.set("items", player.inventory.itemInMainHand)
        config.save(file)
    }

    private fun sendUsage(p0: CommandSender) {
        val message= translate("command.ground.usage")
            .split("|")
        message.forEach {
            p0.sendMessage(it, true)
        }
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): MutableList<String> {
        val tab= mutableListOf<String>()
        if (tab.isEmpty()) {
            tab.addAll(arrayOf(
                "구매아이템설정"
            ))
        }
        if (tab.size==1) {}
        return tab
    }
}