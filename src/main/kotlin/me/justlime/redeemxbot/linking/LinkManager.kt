package me.justlime.redeemxbot.linking

import me.justlime.redeemxbot.RedeemXBot
import me.justlime.redeemxbot.utils.JService
import net.justlime.redeemcodex.dao.AccountLinkDao
import net.justlime.redeemcodex.dao.NotificationToggleDao
import net.justlime.redeemcodex.models.AccountLink
import net.justlime.redeemcodex.models.PendingLink
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LinkManager(
    private val linkDao: AccountLinkDao,
    private val toggleDao: NotificationToggleDao
) {

    // Configuration
    private val config get() = JService.linking
    // Default to 6 digits if config is missing
    private val pinLength: Int get() = config.getInt("linking.pin-length", 6)
    private val expirySeconds: Long get() = config.getLong("linking.code-expiry-seconds", 300)

    // Caches
    private val pendingLinks = ConcurrentHashMap<String, PendingLink>()
    private val uuidToLink = ConcurrentHashMap<UUID, AccountLink>()
    private val discordToLink = ConcurrentHashMap<String, AccountLink>()
    private val notificationCache = ConcurrentHashMap<UUID, Boolean>()

    // Initialization

    fun loadCache() {
        linkDao.getAllLinks { links ->
            links.forEach { cacheLink(it) }
            RedeemXBot.instance.logger.info("Loaded ${links.size} linked accounts from Database.")
        }

        // Cleanup expired PINs every 60 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(RedeemXBot.instance, Runnable {
            val now = System.currentTimeMillis()
            pendingLinks.values.removeIf { it.expiryTime < now }
        }, 1200L, 1200L)
    }

    // --- Core Logic: Linking ---

    fun isPlayerLinked(playerUUID: UUID): Boolean = uuidToLink.containsKey(playerUUID)
    fun isDiscordLinked(discordId: String): Boolean = discordToLink.containsKey(discordId)

    fun getLinkByPlayer(playerUUID: UUID): AccountLink? = uuidToLink[playerUUID]
    fun getLinkByDiscord(discordId: String): AccountLink? = discordToLink[discordId]

    private fun cacheLink(link: AccountLink) {
        uuidToLink[link.playerUUID] = link
        discordToLink[link.discordId] = link
    }

    private fun uncacheLink(playerUUID: UUID) {
        val link = uuidToLink.remove(playerUUID)
        if (link != null) {
            discordToLink.remove(link.discordId)
        }
        notificationCache.remove(playerUUID)
    }

    // --- Core Logic: Process ---

    /**
     * Generates a numeric PIN code (OTP) for the player.
     * Returns NULL if the player is already linked.
     */
    fun generateCode(playerUUID: UUID): PendingLink? {
        // 1. Ignore if already linked
        if (isPlayerLinked(playerUUID)) {
            return null
        }

        // 2. Check if player already has an active pending code
        // (Optional: Reuse existing code or delete old one. Here we delete old ones for that UUID to prevent spam)
        pendingLinks.values.removeIf { it.playerUUID == playerUUID }

        val expiryTime = System.currentTimeMillis() + (expirySeconds * 1000)
        var pinCode: String

        // 3. Generate Unique Numeric PIN
        do {
            pinCode = (1..pinLength)
                .map { (0..9).random() } // Random digit 0-9
                .joinToString("")
        } while (pendingLinks.containsKey(pinCode))

        val pending = PendingLink(playerUUID, pinCode, expiryTime)
        pendingLinks[pinCode] = pending
        return pending
    }

    /**
     * Finalizes the link.
     */
    fun finalizeLink(code: String, discordId: String, playerName: String): AccountLink? {
        val pin = code.trim() // No uppercase needed for numbers, just trim spaces
        val pending = pendingLinks[pin] ?: return null

        if (System.currentTimeMillis() > pending.expiryTime) {
            pendingLinks.remove(pin)
            return null
        }

        // Double check: if user somehow linked in the meantime
        if (isPlayerLinked(pending.playerUUID) || isDiscordLinked(discordId)) {
            pendingLinks.remove(pin)
            return null
        }

        val newLink = AccountLink(pending.playerUUID, playerName, discordId)

        // Update Cache
        cacheLink(newLink)

        // Persist to DB
        linkDao.addLink(newLink) { success ->
            if (!success) {
                RedeemXBot.instance.logger.severe("Failed to save link for $playerName to Database!")
            }
        }

        pendingLinks.remove(pin)
        return newLink
    }

    fun unlink(playerUUID: UUID) {
        if (!isPlayerLinked(playerUUID)) return
        uncacheLink(playerUUID)
        linkDao.removeLink(playerUUID) { }
    }

    // --- Notification Logic ---

    fun shouldNotify(playerUUID: UUID): Boolean = notificationCache.getOrDefault(playerUUID, true)

    fun setNotificationStatus(playerUUID: UUID, enabled: Boolean) {
        notificationCache[playerUUID] = enabled
        toggleDao.setStatus(playerUUID, enabled) { }
    }

    fun onPlayerJoin(playerUUID: UUID) {
        toggleDao.getStatus(playerUUID) { enabled -> notificationCache[playerUUID] = enabled }
    }

    fun onPlayerQuit(playerUUID: UUID) {
        notificationCache.remove(playerUUID)
    }
}