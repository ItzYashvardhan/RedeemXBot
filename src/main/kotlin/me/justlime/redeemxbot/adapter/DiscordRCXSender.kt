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

package me.justlime.redeemxbot.adapter

import api.justlime.redeemcodex.RedeemX
import api.justlime.redeemcodex.adapter.RCXSender
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class DiscordRCXSender(private val event: SlashCommandInteractionEvent) : RCXSender {

    override val name: String = event.user.name

    override fun sendMessage(messageKey: String, placeholders: Map<String, String>) {
        var rawMessage = RedeemX.message.getString(messageKey)
        var rawMessageList = RedeemX.message.getStringList(messageKey)

        placeholders.forEach { (key, value) ->
            rawMessage = rawMessage.replace(key, value)
        }

        if (rawMessageList.isNotEmpty()) {
            rawMessageList = rawMessageList.map { msg ->
                var tempMsg = msg
                placeholders.forEach { (key, value) ->
                    tempMsg = tempMsg.replace(key, value)
                }
                tempMsg
            }
            rawMessage = rawMessageList.joinToString("\n")
        }
        rawMessage = rawMessage.replace("{prefix}" , "")
        rawMessage = rawMessage.replace("{header}" , "")
        rawMessage = rawMessage.replace("{footer}" , "")

        val cleanMessage = rawMessage.replace(Regex("<[^>]*>|&[0-9a-fk-or]"), "")
        if (cleanMessage.isNotEmpty()) {
            if (event.isAcknowledged) {
                event.hook.sendMessage("```" +cleanMessage + "```").queue()
            } else {
                event.reply("```" +cleanMessage+ "```").queue()
            }
        }
    }

    override fun hasPermission(node: String): Boolean = true 
}