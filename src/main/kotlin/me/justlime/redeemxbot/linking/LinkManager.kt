/*
 * RedeemCodeX - Plugin License Agreement
 * Copyright © 2024 Yashvardhan
 *
 * This software is a paid plugin developed by Yashvardhan ("Author") and is provided to you ("User") under the following terms:
 *
 * 1. Usage Rights:
 *    - This plugin is licensed, not sold.
 *    - One license grants usage on **one server network only**, unless explicitly agreed otherwise.
 *    - You may not sublicense, share, leak, or resell the plugin or any part of it.
 *
 * 2. Restrictions:
 *    - You may not decompile, reverse engineer, or modify the plugin.
 *    - You may not redistribute the plugin in any form.
 *    - You may not upload this plugin to any public or private repository or distribution platform.
 *
 * 3. Support & Updates:
 *    - Support is provided to verified buyers only.
 *    - Updates are available as long as development continues or within the support duration stated at purchase.
 *
 * 4. Termination:
 *    - Any violation of this agreement terminates your rights to use this plugin immediately, without refund.
 *
 * 5. No Warranty:
 *    - The plugin is provided "as is", without warranty of any kind. Use at your own risk.
 *    - The Author is not responsible for any damages, data loss, or server issues resulting from usage.
 *
 * For inquiries,
 * Email: itsyashvardhan76@gmail.com
 * Discord: https://discord.gg/rVsUJ4keZN
 */

package me.justlime.redeemxbot.linking

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.justlime.redeemxbot.rxbPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LinkManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val linksFile: File by lazy { File(rxbPlugin.dataFolder, "linked-accounts.json") }

    val config: FileConfiguration by lazy {
        val file = File(rxbPlugin.dataFolder, "linking.yml")
        if (!file.exists()) {
            rxbPlugin.saveResource("linking.yml", false)
        }
        YamlConfiguration.loadConfiguration(file)
    }

    // --- Data Storage ---
    // Temporary codes waiting for a DM from a user.
    private val pendingLinks = ConcurrentHashMap<String, PendingLink>() // Key: Code
    // Completed links.
    private val accountLinksByPlayer = ConcurrentHashMap<UUID, AccountLink>() // Key: Player UUID
    private val accountLinksByDiscord = ConcurrentHashMap<String, AccountLink>() // Key: Discord ID

    fun loadLinks() {
        if (!linksFile.exists()) return

        val type = object : TypeToken<List<AccountLink>>() {}.type
        val links: List<AccountLink> = gson.fromJson(linksFile.readText(), type) ?: emptyList()

        links.forEach { link ->
            accountLinksByPlayer[link.playerUUID] = link
            accountLinksByDiscord[link.discordId] = link
        }
        rxbPlugin.logger.info("Loaded ${links.size} linked accounts.")
    }

    private fun saveLinks() {
        val links = accountLinksByPlayer.values.toList()
        linksFile.writeText(gson.toJson(links))
    }

    // --- Core Logic ---

    fun isPlayerLinked(playerUUID: UUID): Boolean = accountLinksByPlayer.containsKey(playerUUID)
    fun isDiscordLinked(discordId: String): Boolean = accountLinksByDiscord.containsKey(discordId)

    fun getLinkByPlayer(playerUUID: UUID): AccountLink? = accountLinksByPlayer[playerUUID]
    fun getLinkByDiscord(discordId: String): AccountLink? = accountLinksByDiscord[discordId]

    /**
     * Generates a new, unique code for a player to start the linking process.
     */
    fun generateCode(playerUUID: UUID): PendingLink {
        val codeLength = config.getInt("linking.code-length", 6)
        val expirySeconds = config.getLong("linking.code-expiry-seconds", 300)
        val expiryTime = System.currentTimeMillis() + (expirySeconds * 1000)

        // Generate a unique code
        var code: String
        do {
            code = (1..codeLength)
                .map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }
                .joinToString("")
        } while (pendingLinks.containsKey(code))

        val pendingLink = PendingLink(playerUUID, code, expiryTime)
        pendingLinks[code] = pendingLink
        return pendingLink
    }

    /**
     * Attempts to finalize a link using a code provided via Discord DM.
     */
    fun finalizeLink(code: String, discordId: String, playerName: String): AccountLink? {
        val pending = pendingLinks[code.uppercase()] ?: return null

        // Check for expiry
        if (System.currentTimeMillis() > pending.expiryTime) {
            pendingLinks.remove(code.uppercase())
            return null
        }

        // Create and store the final link
        val newLink = AccountLink(pending.playerUUID, playerName, discordId)
        accountLinksByPlayer[pending.playerUUID] = newLink
        accountLinksByDiscord[discordId] = newLink

        // Clean up and save
        pendingLinks.remove(code.uppercase())
        saveLinks()

        return newLink
    }
}
