package kr.apo2073.chunkly.papi

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.LangManager.translate
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlaceHolderHandler : PlaceholderExpansion() {
    override fun getIdentifier(): String = "chunkly"
    override fun getAuthor(): String = Chunkly.plugin.pluginMeta.authors.joinToString(", ")
    override fun getVersion(): String = Chunkly.plugin.pluginMeta.version

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null

        val chunks = Chunks(player.chunk)
        return when {
            params == "canbuy" -> chunks.canBuy().toString()
            params == "owner" -> chunks.getOwner() ?: translate("placeholder.owner.null")
            params.startsWith("share") -> {
                val parts = params.split("_")
                if (parts.size != 3) return null

                val ownerName = parts[1]
                val index = parts[2].toIntOrNull() ?: return null
                val owner = Bukkit.getOfflinePlayer(ownerName)

                val cacheKey = "share_${owner.uniqueId}_$index"
                val cachedValue = PlaceholderCache.get(cacheKey)
                if (cachedValue != null) return cachedValue

                Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
                    UserData.getConfig(owner.uniqueId) { config ->
                        val shareList = config.getStringList("user.share-permissions")
                        val value = shareList.getOrNull(index - 1) ?: "N/A"
                        PlaceholderCache.set(cacheKey, value)
                    }
                })
                ""
            }
            else -> null
        }
    }
}

object PlaceholderCache {
    private val cache = mutableMapOf<String, String>()

    fun get(key: String): String? = cache[key]
    fun set(key: String, value: String) {
        cache[key] = value
    }
}
