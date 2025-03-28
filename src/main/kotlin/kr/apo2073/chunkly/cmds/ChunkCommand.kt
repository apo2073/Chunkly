package kr.apo2073.chunkly.cmds

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import kr.apo2073.chunkly.utils.EconManager
import kr.apo2073.chunkly.utils.LangManager.translate
import kr.apo2073.chunkly.utils.asynchronously
import kr.apo2073.chunkly.utils.prefix
import kr.apo2073.chunkly.utils.sendMessage
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
                "reload" -> asynchronously { Bukkit.reloadData() }
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
        val chunks = Chunks(player.chunk)
        asynchronously {
            val canBuy = chunks.canBuy()
            chunks.setCanBuy(!canBuy)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(
                    translate(if (canBuy) "command.ground.setcanbuy.false" else "command.ground.setcanbuy.true"),
                    true
                )
            })
        }
    }

    private fun performGiveItem(p0: Player, p3: Array<out String>) {
        if (!p0.hasPermission("apo.chunkly.set.item")) {
            p0.sendMessage(translate("command.no.permission"), true)
            return
        }

        asynchronously {
            val config = YamlConfiguration.loadConfiguration(getConfigFile("items"))
            val item = config.getItemStack("items") ?: run {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    p0.sendMessage(translate("command.ground.cant.buy"), true)
                })
                return@asynchronously
            }
            val player = Bukkit.getPlayer(p3[1]) ?: return@asynchronously
            val amount = p3[2].toIntOrNull() ?: return@asynchronously

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
        }
    }

    private fun performOwning(p0: Player, p3: Array<out String>) {
        // TODO: 소유권 이전 로직 추가 필요 (현재 미완성)
        sendUsage(p0)
    }

    private fun performPermission(p0: CommandSender, p3: Array<out String>) {
        val execute = p3[1]
        val target = Bukkit.getPlayer(p3[2]) ?: return
        val player = p0 as Player

        asynchronously {
            when (execute) {
                "추가" -> {
                    UserData.addMember(target, player.uniqueId)
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        p0.sendMessage(
                            translate("command.ground.member.add").replace("{player}", target.name),
                            true
                        )
                    })
                }
                "제거" -> {
                    UserData.removeMember(target, player.uniqueId)
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        p0.sendMessage(
                            translate("command.ground.member.remove").replace("{player}", target.name),
                            true
                        )
                    })
                }
            }
        }
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

        asynchronously {
            val file = getConfigFile("items")
            val config = YamlConfiguration.loadConfiguration(file)
            config.set("items", item)
            config.save(file)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.sendMessage(translate("command.ground.set.item"), true)
            })
        }
    }

    private fun performRemoveChunk(p0: Player, chunkKey: String) {
        asynchronously {
            val file = File("${plugin.dataFolder}/chunkdata", chunkKey)
            if (!file.exists()) return@asynchronously

            val config = YamlConfiguration.loadConfiguration(file)
            val chunk = Bukkit.getWorld(UUID.fromString(config.getString("chunk.world")))
                ?.getChunkAt(config.getInt("chunk.location.x"), config.getInt("chunk.location.z"))
                ?: return@asynchronously
            val owner = Bukkit.getPlayer(Chunks(chunk).getOwner().toString())

            EconManager.sellChunk(chunk, null)
            file.delete()

            Bukkit.getScheduler().runTask(plugin, Runnable {
                owner?.sendMessage(translate("command.ground.force.sell.owner"), true)
                p0.sendMessage(translate("command.ground.force.sell"), true)
                chunk.persistentDataContainer.remove(NamespacedKey(plugin, "owner"))
                performList(p0)
            })
        }
    }

    private fun performList(p0: CommandSender) {
        if (p0 !is Player) return
        val msg = translate("command.ground.list").split("|")
        p0.sendMessage(msg[0], true)

        asynchronously {
            if (p0.isOp) {
                val chunkList= File(plugin.dataFolder, "chunkdata").listFiles()?.map { it.name } ?: emptyList()
                if (chunkList.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        p0.sendMessage(translate("command.ground.list.no"), true)
                    })
                    return@asynchronously
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    chunkList.forEach { chunk ->
                        if (p0.isOp) {
                            p0.sendMessage(prefix.append(
                                Component.text(msg[1].replace("{list}", chunk.replace(".yml", "")) + " [ " +
                                        Chunks(getChunk(chunk.replace(".yml", "").toLong()) ?: return@forEach).getOwner() + " ]")
                                    .append(Component.text(" [TP]")
                                        .color(NamedTextColor.GRAY)
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                run {
                                                    val chunkData = getChunk(chunk.toLong()) ?: return@run ""
                                                    val x = (chunkData.x shl 4) + 8
                                                    val z = (chunkData.z shl 4) + 8
                                                    val y = chunkData.world.getHighestBlockYAt(x, z)
                                                    p0.teleport(Location(chunkData.world, x.toDouble(), y.toDouble(), z.toDouble()))
                                                    ""
                                                }
                                            )
                                        ))
                                    .append(Component.text(" [제거]")
                                        .color(NamedTextColor.RED)
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/땅 removeChunk $chunk"
                                            )
                                        ))
                            ))
                        } else {
                            p0.sendMessage("${msg[1].replace("{list}", chunk)} &7${"[ &a" + Chunks(
                                getChunk(chunk.toLong()) ?: return@forEach
                            ).getOwner() + "&7]"}", true)
                        }
                    }
                })
            } else {
                if (!p0.hasPermission("apo.chunkly.cmds")) {
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        p0.sendMessage(translate("command.no.permission"), true)
                    })
                    return@asynchronously
                }
                UserData.getConfig(p0.uniqueId) {
                    val chunkList= it.getStringList("user.has-chunk")

                    if (chunkList.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            p0.sendMessage(translate("command.ground.list.no"), true)
                        })
                        return@getConfig
                    }

                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        chunkList.forEach { chunk ->
                            if (p0.isOp) {
                                p0.sendMessage(prefix.append(
                                    Component.text(msg[1].replace("{list}", chunk.replace(".yml", "")) + " [ " +
                                            Chunks(getChunk(chunk.replace(".yml", "").toLong()) ?: return@forEach).getOwner() + " ]")
                                        .append(Component.text(" [TP]")
                                            .color(NamedTextColor.GRAY)
                                            .clickEvent(
                                                ClickEvent.clickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    run {
                                                        val chunkData = getChunk(chunk.toLong()) ?: return@run ""
                                                        val x = (chunkData.x shl 4) + 8
                                                        val z = (chunkData.z shl 4) + 8
                                                        val y = chunkData.world.getHighestBlockYAt(x, z)
                                                        p0.teleport(Location(chunkData.world, x.toDouble(), y.toDouble(), z.toDouble()))
                                                        ""
                                                    }
                                                )
                                            ))
                                        .append(Component.text(" [제거]")
                                            .color(NamedTextColor.RED)
                                            .clickEvent(
                                                ClickEvent.clickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/땅 removeChunk $chunk"
                                                )
                                            ))
                                ))
                            } else {
                                p0.sendMessage("${msg[1].replace("{list}", chunk)} &7${"[ &a" + Chunks(
                                    getChunk(chunk.toLong()) ?: return@forEach
                                ).getOwner() + "&7]"}", true)
                            }
                        }
                    })
                }
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

    private fun getChunk(chunkKey: Long): Chunk? {
        val file = File("${Chunkly.plugin.dataFolder}/chunkdata", chunkKey.toString())
        if (!file.exists()) return null
        val config = YamlConfiguration.loadConfiguration(file)
        return Bukkit.getWorld(UUID.fromString(config.getString("chunk.world")))
            ?.getChunkAt(config.getInt("chunk.location.x"), config.getInt("chunk.location.z"))
    }
}