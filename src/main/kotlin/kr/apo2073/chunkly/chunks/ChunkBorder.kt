package kr.apo2073.chunkly.chunks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import kr.apo2073.chunkly.Chunkly
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class ChunkBorder {

    internal val enabledPlayers = mutableSetOf<UUID>()
    private val protocolManager = Chunkly.protocolManager

    fun startParticleTask() {
        object : BukkitRunnable() {
            override fun run() {
                enabledPlayers.forEach { playerId ->
                    Bukkit.getServer().getPlayer(playerId)?.let { player ->
                        if (player.isOnline) showChunkBorders(player).also { println(player.name) }
                    }
                }
            }
        }.runTaskTimer(Chunkly.plugin, 0L, 10L)
    }

    private fun showChunkBorders(player: Player) {
        val loc = player.location
        val chunkX = loc.blockX shr 4
        val chunkZ = loc.blockZ shr 4

        val minX = chunkX * 16
        val maxX = minX + 15
        val minZ = chunkZ * 16
        val maxZ = minZ + 15
        val y = loc.blockY.toDouble()

        for (x in minX..maxX) {
            sendParticle(player, x + 0.5, y, minZ + 0.5)
            sendParticle(player, x + 0.5, y, maxZ + 0.5)
        }
        for (z in minZ..maxZ) {
            sendParticle(player, minX + 0.5, y, z + 0.5)
            sendParticle(player, maxX + 0.5, y, z + 0.5)
        }
    }

    private fun sendParticle(player: Player, x: Double, y: Double, z: Double) {
        val packet = PacketContainer(PacketType.Play.Server.WORLD_PARTICLES)
        packet.doubles
            .write(0, x)
            .write(1, y)
            .write(2, z)
        packet.float
            .write(0, 0.0f)
            .write(1, 0.0f)
            .write(2, 0.0f)
            .write(3, 0.0f)
        packet.integers.write(0, 50)
        packet.particles.write(0, EnumWrappers.Particle.FLAME)

        protocolManager.sendServerPacket(player, packet) // not work
    }


}