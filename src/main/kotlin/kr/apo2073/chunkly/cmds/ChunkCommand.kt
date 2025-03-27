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
import org.bukkit.Chunk
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
                "reload"-> {
                    Bukkit.reloadData()
                }
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
            performList(p0)
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
        p0.sendMessage(translate("command.ground.give.item")
            .replace("{player}", player.name)
            .replace("{amount}", amount.toString()), true)
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
        player.sendMessage(translate("command.ground.set.item"), true)
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
                    Component.text(msg[1].replace("{list}", chunk.replace(".yml", "")) + " [ " +
                            Chunks(getChunk(chunk.toLong()) ?: return@forEach).getOwner() + " ]")
                        .append(Component.text(" [TP]")
                            .color(NamedTextColor.GRAY)
                            .clickEvent(
                                ClickEvent.clickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    run {
                                        val chunkData = getChunk(chunk.toLong()) ?: return@run "/minecraft:tp 0 0 0" // 청크가 없으면 기본값 처리
                                        val x = (chunkData.x shl 4) + 8
                                        val z = (chunkData.z shl 4) + 8
                                        val y = chunkData.world.getHighestBlockYAt(x, z)
                                        "/minecraft:tp $x $y $z"
                                    }
                                )
                            )
                        )
                        .append(Component.text(" [제거]")
                            .color(NamedTextColor.RED)
                            .clickEvent(
                                ClickEvent.clickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/땅 removeChunk $chunk"
                                )
                            )
                        )
                ))
            } else {
                p0.sendMessage("${msg[1].replace("{list}", chunk)} &7${"[ &a"+Chunks(
                    getChunk(chunk.toLong()) ?: return@forEach
                ).getOwner() + "&7]"}", true)
            }
        }
    }

    private fun sendUsage(p0: CommandSender) {
        val message= if (p0.isOp) {
            translate("command.ground.usage.op")
        } else {translate("command.ground.usage.deop")}
            .split("|")
        message.forEach {
            p0.sendMessage(it, true)
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val tab = mutableListOf<String>()

        if (args.size == 1) {
            val baseCommands = listOf("목록", "권한", "아이템지급")
            tab.addAll(baseCommands)
            if (sender.hasPermission("apo.chunkly.set.item")) {
                tab.add("구매아이템설정")
            }
            return tab.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }

        if (sender !is Player) return tab

        if (args.size == 2) {
            when (args[0]) {
                "소유권이전" -> {
                    tab.addAll(UserData.getConfig(sender.uniqueId).getStringList("has-chunk"))
                }
                "권한" -> {
                    tab.addAll(listOf("추가", "제거"))
                }
                "아이템지급" -> {
                    tab.addAll(plugin.server.onlinePlayers.map { it.name })
                }
            }
            return tab.filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
        }

        if (args.size == 3) {
            when (args[0]) {
                "소유권이전", "권한" -> {
                    tab.addAll(plugin.server.onlinePlayers.map { it.name })
                }
                "아이템지급" -> {
                    tab.addAll(listOf("1", "16", "32", "64"))
                }
            }
            return tab.filter { it.startsWith(args[2], ignoreCase = true) }.toMutableList()
        }

        return tab
    }

    private fun getChunk(chunkKey:Long): Chunk? {
        val file= File("${Chunkly.plugin.dataFolder}/chunkdata", chunkKey.toString())
        if (!file.exists()) return null
        val config= YamlConfiguration.loadConfiguration(file)
        val chunk=Bukkit
            .getWorld(UUID.fromString(config.getString("chunk.world")))?.getChunkAt(
                config.getInt("chunk.location.x"),
                config.getInt("chunk.location.z")
            ) ?: return null
        return chunk
    }
}