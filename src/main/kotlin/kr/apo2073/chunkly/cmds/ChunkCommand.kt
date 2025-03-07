package kr.apo2073.chunkly.cmds

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.prefix
import kr.apo2073.chunkly.utils.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

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
                else-> sendUsage(p0)
            }
        }
        if (p3.size==2 && p3[0]=="removeChunk") {
            val chunkKey=p3[1]
            val file=File("${plugin.dataFolder}/chunkdata", chunkKey)
            if (!file.exists()) return true
            val config=YamlConfiguration.loadConfiguration(file)
            val chunk=Bukkit
                .getWorld(UUID.fromString(config.getString("chunk.world")))?.getChunkAt(
                    config.getInt("chunk.location.x"),
                    config.getInt("chunk.location.z")
                ) ?: return true
            val owner=Bukkit.getPlayer(Chunks(chunk).getOwner().toString())
            owner?.sendMessage(translate("command.ground.force.sell.owner"), true)
            p0.sendMessage(translate("command.ground.force.sell"), true)
            EconManager.sellChunk(chunk, null)
            chunk.persistentDataContainer.remove(NamespacedKey(plugin, "owner"))
            file.delete()
        }
        if (p3.size==2) {
            when(p3[0]) {
                "소유권이전"->performOwning(p0, p3)
            }
        }
        if (p3.size==3) {
            when(p3[0]) {
                "권한"->performPermission(p0, p3)
                "아이템지급"->performGiveItem(p0, p3)
            }
        }
        return true
    }

    private fun performGiveItem(p0: Player, p3: Array<out String>) {
        val config= YamlConfiguration.loadConfiguration(getConfigFile("items"))
        val item=config.getItemStack("items") ?: return
        if (!p0.hasPermission("apo.chunkly.set.item")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }
        p0.sendMessage(translate("command.ground.cant.buy"), true)
        val player=Bukkit.getPlayer(p3[1]) ?: return
        val amount=p3[2].toIntOrNull() ?: return
        item.amount=amount
        player.inventory.addItem(item)
    }

    private fun performOwning(p0: Player, p3: Array<out String>) {
//        EconManager.sellChunk(p0.chunk, p3[])
        sendUsage(p0)
    }

    private fun performPermission(p0: CommandSender, p3: Array<out String>) {
        val execute=p3[1]
        val player= Bukkit.getPlayer(p3[2]) ?: return
        if (execute=="추가") {
            p0.sendMessage(translate("command.ground.member.add").replace("{player}", player.name), true)
            UserData.addMember(player, (p0 as Player).uniqueId)
        }
        if (execute=="제거") {
            p0.sendMessage(translate("command.ground.member.remove").replace("{player}", player.name), true)
            UserData.removeMember(player, (p0 as Player).uniqueId)
        }
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
        if (p0 !is Player) return
        val msg = translate("command.ground.list").split("|")
        p0.sendMessage(msg[0], true)

        val chunkList = if (p0.isOp) {
            File(plugin.dataFolder, "chunkdata").listFiles()?.map { it.name } ?: emptyList()
        } else {
            if (!p0.hasPermission("apo.chunkly.cmds")) {
                p0.sendMessage(translate("command.no.permission"), true)
                return
            }
            UserData.getConfig(p0.uniqueId).getStringList("user.has-chunk")
        }

        if (chunkList.isEmpty()) {
            p0.sendMessage(translate("command.ground.list.no"), true)
            return
        }

        chunkList.forEach { chunk ->
//            p0.sendMessage(msg[1].replace("{list}", chunk), true)
            if (p0.isOp) {
                p0.sendMessage(prefix.append(
                    Component.text(msg[1].replace("{list}",
                        chunk.replace(".yml", "")))
                        .append(Component.text(" [제거]")
                            .color(NamedTextColor.RED)
                            .clickEvent(
                                ClickEvent.clickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/땅 removeChunk $chunk"
                                )))))
            } else {
                val config=YamlConfiguration.loadConfiguration(getConfigFile(chunk))
                val x=config.getInt("chunk.location.x")
                val z=config.getInt("chunk.location.z")
                p0.sendMessage("${msg[1].replace("{list}", chunk)} &7[ &a${x}&f, &a${z} &7]", true)
            }
        }
    }

    private fun sendUsage(p0: CommandSender) {
        val message= translate("command.ground.usage")
            .split("|")
        message.forEach {
            if (it.contains("아이템")) return@forEach
            if (it.contains(""))
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
//                "소유권이전",
                "권한",
                "아이템지급"
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
            if (p3[0]=="아이템지급") {
                tab.addAll(arrayOf("1", "16", "32", "64"))
            }
        }
        return tab
    }
}