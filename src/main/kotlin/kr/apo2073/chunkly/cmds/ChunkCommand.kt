package kr.apo2073.chunkly.cmds

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.sendMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ChunkCommand(plugin: JavaPlugin): TabExecutor {
    private val plugin=Chunkly.plugin
    init {
        plugin.getCommand("땅")?.apply {
            setExecutor(this@ChunkCommand)
            tabCompleter=this@ChunkCommand
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
                "목록"-> performList(p0)
                "소유권이전"-> performOwning(p0, p3)
                else-> sendUsage(p0)
            }

            return true
        }
        return true
    }

    private fun performOwning(p0: Player, p3: Array<out String>) {
//        EconManager.sellChunk(p0.chunk, p3[])
    }

    private fun performItem(p0: CommandSender) {
        if (!p0.hasPermission("apo.chunkly.set.item")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }
        val player=p0 as Player
        if (player.inventory.itemInMainHand.isEmpty) {
            player.sendMessage(translate("command.ground.held.item"), true)
            return
        }
        val file=getConfigFile("items")
        val config=YamlConfiguration.loadConfiguration(file)
        config.set("items", player.inventory.itemInMainHand)
        config.save(file)
    }

    private fun performList(p0: CommandSender) {
        if (!p0.hasPermission("apo.chunkly.cmds")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }
        val player=p0 as Player
        val list=UserData.getConfig(player.uniqueId).getStringList("has-chunk")
        val msg= translate("command.ground.list").split("|")
        player.sendMessage(msg[0], true)
        for (chunk in list)
            player.sendMessage(msg[1].replace("{list}", chunk), true)
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
//                "구매아이템설정",
                "목록",
                "소유권이전",
                "권한"
            ))
            if (p0.hasPermission("apo.chunkly.set.item")) tab.add("구매아이템설정")
        }
        if (tab.size==1) {
            if (p3[0]=="소유권이전") {
                tab.addAll(UserData.getConfig((p0 as Player).uniqueId).getStringList("has-chunk"))
            }
            if (p3[0]=="권한") {
                tab.addAll(arrayOf(
                    "추가",
                    "제거"
                ))
            }
        }
        if (tab.size==2) {
            if (p3[0]=="소유권이전" || p3[0]=="권한") {
                tab.addAll(plugin.server.onlinePlayers.map { it.name })
            }
        }
        return tab
    }
}