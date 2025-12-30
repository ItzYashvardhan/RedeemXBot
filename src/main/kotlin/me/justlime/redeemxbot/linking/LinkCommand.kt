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

import me.justlime.redeemxbot.rxbPlugin
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LinkCommand(private val linkManager: LinkManager) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        if (linkManager.isPlayerLinked(sender.uniqueId)) {
            val message = linkManager.config.getString("linking.already-linked-ingame", "&cYour account is already linked.")
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message ?: ""))
            return true
        }

        val pendingLink = linkManager.generateCode(sender.uniqueId)
        val botName = rxbPlugin.getJDA().selfUser.name

        val messages = linkManager.config.getStringList("linking.code-message")
        messages.forEach { line ->
            val formattedLine = line
                .replace("{code}", pendingLink.code)
                .replace("{bot_name}", botName)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedLine))
        }

        return true
    }
}
