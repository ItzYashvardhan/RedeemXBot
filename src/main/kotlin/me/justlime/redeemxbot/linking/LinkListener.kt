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

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import org.bukkit.ChatColor

class LinkListener(private val linkManager: LinkManager) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // Only handle DMs and ignore other bots
        if (!event.isFromGuild && !event.author.isBot) {
            val code = event.message.contentRaw.trim()
            val discordUser = event.author
            val discordId = discordUser.id

            // Check if the user is already linked
            if (linkManager.isDiscordLinked(discordId)) {
                val message = linkManager.config.getString("linking.already-linked-discord", "Your account is already linked.")
                discordUser.openPrivateChannel().queue { it.sendMessage(message!!).queue() }
                return
            }

            // Attempt to finalize the link
            val playerUUID = linkManager.getLinkByDiscord(discordId)?.playerUUID
            val player = playerUUID?.let { Bukkit.getOfflinePlayer(it) }
            val playerName = player?.name ?: "Unknown"

            val newLink = linkManager.finalizeLink(code, discordId, playerName)

            if (newLink != null) {
                // Success! Send confirmation messages.
                val successDiscordMsg = linkManager.config.getString("linking.success-discord", "Success! Linked to **{player_name}**.")
                    ?.replace("{player_name}", newLink.playerName)
                discordUser.openPrivateChannel().queue { it.sendMessage(successDiscordMsg!!).queue() }

                // Also send a message to the player in-game if they are online
                val onlinePlayer = Bukkit.getPlayer(newLink.playerUUID)
                if (onlinePlayer != null && onlinePlayer.isOnline) {
                    val successIngameMsg = linkManager.config.getString("linking.success-ingame", "&aSuccess! Linked to &b{discord_name}.")
                        ?.replace("{discord_name}", "${discordUser.name}#${discordUser.discriminator}")
                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', successIngameMsg!!))
                }

            } else {
                // Failure - invalid or expired code
                val invalidCodeMsg = linkManager.config.getString("linking.invalid-code-discord", "Invalid or expired code.")
                discordUser.openPrivateChannel().queue { it.sendMessage(invalidCodeMsg!!).queue() }
            }
        }
    }
}
