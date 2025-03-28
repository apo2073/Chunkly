package kr.apo2073.chunkly.events

import com.destroystokyo.paper.event.player.*
import io.papermc.paper.event.player.*
import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.data.UserData
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PlayerChunkInteraction : Listener {
    private val plugin = Chunkly.plugin

    private fun hasPermission(chunk: Chunk, player: Player, callback: (Boolean) -> Unit) {
        val owner = chunk.persistentDataContainer.get(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING
        )
        if (owner == null) {
            callback(true)
            return
        }
        if (owner == player.uniqueId.toString() || player.isOp) {
            callback(true)
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            UserData.getMember(UUID.fromString(owner)) {
                val hasPermission = it.contains(player.uniqueId)
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    callback(hasPermission)
                })
            }
        })
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerInteractEvent.onInteraction() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerHarvestBlockEvent.onHarvest() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerAttemptPickupItemEvent.onPickUp() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBedEnterEvent.onEnter() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketFillEvent.onBucket() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketEmptyEvent.onBucket() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketEntityEvent.onBucket() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerLaunchProjectileEvent.onLaunch() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerItemFrameChangeEvent.onFrameChange() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerSetSpawnEvent.onSetSpawn() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerDropItemEvent.onDrop() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerItemMendEvent.onMend() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityPlaceEvent.onPlace() {
        val entityPlayer = player ?: return
        hasPermission(entityPlayer.chunk, entityPlayer) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerAdvancementCriterionGrantEvent.onAdvancementGrant() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerChangeBeaconEffectEvent.onBeaconChange() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun ProjectileHitEvent.onProjectileHit() {
        val shooter = entity.shooter as? Player ?: return
        hasPermission(shooter.chunk, shooter) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBlockBreak() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPlaceEvent.onBlockPlace() {
        hasPermission(player.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityDamageByEntityEvent.onEntityDamage() {
        val damaged = damager as? Player ?: return
        hasPermission(damaged.chunk, damaged) { allowed ->
            if (!allowed) isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityExplodeEvent.onEntityExplode() {
        val player = location.world.players.firstOrNull { it.location.chunk == location.chunk } ?: return
        hasPermission(location.chunk, player) { allowed ->
            if (!allowed) isCancelled = true
        }
    }
}