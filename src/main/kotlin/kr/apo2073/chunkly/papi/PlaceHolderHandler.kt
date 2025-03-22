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

        val chunks = Chunks(player.x, player.z, player.world)
        return when {
            params == "canbuy" -> chunks.canBuy().toString()
            params == "owner" -> chunks.getOwner() ?: translate("placeholder.owner.null")
            params.startsWith("share") -> {
                println(params)
                val parts = params.split("_")
                if (parts.size != 3) return null
                println(parts.joinToString("_"))

                val ownerName = parts[1]
                val index = parts[2].toIntOrNull() ?: return null

                val owner = Bukkit.getOfflinePlayer(ownerName)
                val shareList = UserData.getConfig(owner.uniqueId).getStringList("user.share-permissions")
                shareList.getOrNull(index-1) ?: "N/A"
            }
            else -> null
        }
    }
}