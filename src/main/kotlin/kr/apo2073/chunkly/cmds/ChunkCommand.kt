package kr.apo2073.chunkly.cmds

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.prefix
import kr.apo2073.chunkly.utils.sendMessage
import kr.apo2073.chunkly.utils.str2Component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*
import kotlin.collections.mutableListOf

class ChunkCommand(plugin: JavaPlugin) : TabExecutor {
    private val plugin = Chunkly.plugin

    init {
        plugin.getCommand("땅")?.apply {
            setExecutor(this@ChunkCommand)
            tabCompleter = this@ChunkCommand
        }
    }

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return true
        if (p3.isEmpty()) {
            sendUsage(p0)
            return true
        }

        when (p3.size) {
            1 -> when (p3[0]) {
                "구매아이템설정" -> performItem(p0)
                "목록" -> performList(p0)
                "구매불가설정" -> performCanBuy(p0)
                "reload" -> Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable { Bukkit.reloadData() })
                else -> sendUsage(p0)
            }
            2 -> when (p3[0]) {
                "removeChunk" -> performRemoveChunk(p0, p3[1])
                "소유권이전" -> performOwning(p0, p3)
            }
            3 -> when (p3[0]) {
                "권한" -> performPermission(p0, p3)
                "아이템지급" -> performGiveItem(p0, p3)
            }
        }
        return true
    }

    private fun performCanBuy(player: Player) {
        if (!(player.isOp)) return
        val chunks = Chunks(player.chunk)
        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            val uuid= Bukkit.getPlayer(chunks.getOwner().toString())?.uniqueId
            if (uuid!=null) UserData.removeChunk(chunks.getChunk()?.chunkKey.toString(), uuid)
            val canBuy = chunks.canBuy()
            chunks.setCanBuy(!canBuy)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(
                    translate(if (canBuy) "command.ground.setcanbuy.false" else "command.ground.setcanbuy.true"),
                    true
                )
            })
        })
    }

    private fun performGiveItem(p0: Player, p3: Array<out String>) {
        if (!p0.hasPermission("apo.chunkly.set.item")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            val config = YamlConfiguration.loadConfiguration(getConfigFile("items"))
            val item = config.getItemStack("items") ?: run {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    p0.sendMessage(translate("command.ground.cant.buy"), true)
                })
                return@Runnable
            }

            val player = Bukkit.getPlayer(p3[1])
            val amount = p3[2].toIntOrNull()

            if (player == null || amount == null) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    p0.sendMessage(translate("command.ground.invalid.args"), true)
                })
                return@Runnable
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                p0.sendMessage(
                    translate("command.ground.give.item")
                        .replace("{player}", player.name)
                        .replace("{amount}", amount.toString()),
                    true
                )
                item.amount = amount
                player.inventory.addItem(item)
            })
        })
    }

    private fun performOwning(p0: Player, p3: Array<out String>) {
        sendUsage(p0) // 소유권 이전 로직이 미완성인 상태로 두었습니다.
    }

    private fun performPermission(p0: CommandSender, p3: Array<out String>) {
        val execute = p3[1]
        val target = Bukkit.getPlayer(p3[2])
        val player = p0 as Player

        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            if (target==null) {
                player.sendMessage(translate("command.not.exist.player"), true)
                return@Runnable
            }
            if (target==player) {
                player.sendMessage(translate("command.ground.member.me"), true)
                return@Runnable
            }
            UserData.getMember(player.uniqueId) {
                when (execute) {
                    "추가" -> {
                        if (it.contains(target.uniqueId)) {
                            player.sendMessage(translate("command.ground.member.add.exist").replace("{player}", target.name), true)
                            return@getMember
                        }
                        UserData.addMember(target, player.uniqueId)
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            p0.sendMessage(translate("command.ground.member.add").replace("{player}", target.name), true)
                        })
                    }
                    "제거" -> {
                        if (it.contains(target.uniqueId).not()) {
                            player.sendMessage(translate("command.ground.member.remove.not.exist").replace("{player}", target.name), true)
                            return@getMember
                        }
                        UserData.removeMember(target, player.uniqueId)
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            p0.sendMessage(translate("command.ground.member.remove").replace("{player}", target.name), true)
                        })
                    }
                }
            }
        })
    }

    private fun performItem(p0: CommandSender) {
        if (!p0.hasPermission("apo.chunkly.set.item")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }
        val player = p0 as Player
        val item = player.inventory.itemInMainHand
        if (item.isEmpty) {
            player.sendMessage(translate("command.ground.held.item"), true)
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            val file = getConfigFile("items")
            val config = YamlConfiguration.loadConfiguration(file)
            config.set("items", item)
            config.save(file)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(translate("command.ground.set.item"), true)
            })
        })
    }

    private fun performRemoveChunk(p0: Player, chunkKey: String) {
        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            if (!p0.isOp) return@Runnable

            val file = File("${plugin.dataFolder}/chunkdata", "${chunkKey}.yml")
            if (!file.exists()) return@Runnable

            val config = YamlConfiguration.loadConfiguration(file)
            val chunk = Bukkit.getWorld(UUID.fromString(config.getString("chunk.world")))?.getChunkAt(config.getInt("chunk.location.x"), config.getInt("chunk.location.z"))
                ?: return@Runnable

            val owner = Bukkit.getPlayer(Chunks(chunk).getOwner().toString()) ?: return@Runnable

            UserData.removeChunk(chunk.chunkKey.toString(), owner.uniqueId)
            EconManager.sellChunk(chunk, null)
            file.delete()

            Bukkit.getScheduler().runTask(plugin, Runnable {
                owner.sendMessage(translate("command.ground.force.sell.owner"), true)
                p0.sendMessage(translate("command.ground.force.sell"), true)
                chunk.persistentDataContainer.remove(NamespacedKey(plugin, "owner"))
                performList(p0)
            })
        })
    }

    private fun performList(p0: CommandSender) {
        if (p0 !is Player) return
        if (!p0.hasPermission("apo.chunkly.cmds")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }

        val msg = translate("command.ground.list").split("|")
        p0.sendMessage(msg[0], true)

        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            if (p0.isOp) {
                val chunkList = File(plugin.dataFolder, "chunkdata").listFiles()?.map { it.name.replace(".yml", "") } ?: emptyList()
                sendChunkList(p0, chunkList, msg)
            } else {
                UserData.getConfig(p0.uniqueId) { config ->
                    val chunkList = config.getStringList("user.has-chunk")
                    sendChunkList(p0, chunkList, msg)
                }
            }
        })
    }

    private fun sendChunkList(p0: Player, chunkList: List<String>, msg: List<String>) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (chunkList.isEmpty()) {
                p0.sendMessage(translate("command.ground.list.no"), true)
                return@Runnable
            }
            chunkList.forEach { chunk ->
                if (p0.isOp) {
                    val chunkData = getChunk(chunk.toLong()) ?: return@forEach
                    p0.sendMessage(prefix.append(
                        "${msg[1].replace("{list}", chunk)} [ &a${Chunks(chunkData).getOwner()}&b ]".str2Component()
                            .append(Component.text(" [TP]")
                                .color(NamedTextColor.GRAY)
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, run {
                                    val x = (chunkData.x shl 4) + 8
                                    val z = (chunkData.z shl 4) + 8
                                    val y = chunkData.world.getHighestBlockYAt(x, z) + 1
                                    "/minecraft:tp @s $x $y $z"
                                }))
                                .append(Component.text(" [제거]")
                                    .color(NamedTextColor.RED)
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/땅 removeChunk $chunk")))
                            )))
                } else {
                    val chunkData = getChunk(chunk.toLong()) ?: return@forEach
                    p0.sendMessage("${msg[1].replace("{list}", chunk)} &7[ &ax&7: &f${(chunkData.x shl 4) + 8}, &cz&7: &f${(chunkData.z shl 4) + 8} &7]", true)
                }
            }
        })
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

    private fun getChunk(chunkKey: Long): Chunk? {
        val file = File("${Chunkly.plugin.dataFolder}/chunkdata", "$chunkKey.yml")
        if (!file.exists()) return null
        val config = YamlConfiguration.loadConfiguration(file)
        return Bukkit.getWorld(UUID.fromString(config.getString("chunk.world")))
            ?.getChunkAt(config.getInt("chunk.location.x"), config.getInt("chunk.location.z"))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val tab = mutableListOf<String>()

        if (args.size == 1) {
            val baseCommands = listOf("목록", "권한")
            tab.addAll(baseCommands)
            if (sender.hasPermission("apo.chunkly.set.item")) {
                tab.addAll(listOf("구매아이템설정", "아이템지급", "구매불가설정"))
            }
            return tab.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }

        if (sender !is Player) return tab

        if (args.size == 2) {
            when (args[0]) {
                "소유권이전" -> {
                    UserData.getConfig(sender.uniqueId) {
                        tab.addAll(it.getStringList("has-chunk"))
                    }
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
}
