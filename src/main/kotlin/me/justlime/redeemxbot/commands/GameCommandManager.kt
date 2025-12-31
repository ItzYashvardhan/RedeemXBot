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

package me.justlime.redeemxbot.commands

import me.justlime.redeemxbot.RedeemXBot
import me.justlime.redeemxbot.utils.JService
import net.justlime.redeemcodex.enums.JConfig

class GameCommandManager(plugin: RedeemXBot) {
    val commandHelper = PluginCommandRedefiner(plugin)
    private val linkCommand = LinkCommand(plugin)
    private val linkCommandCompletion = LinkCommand(plugin)

    init {

        val linkCommandAliases = JService.linking.getStringList(JConfig.Linking.COMMAND_ALIASES)
        linkCommandAliases.add(0, "codelink")

        plugin.getCommand("codelink")?.apply {
            setExecutor(linkCommand)
            tabCompleter = linkCommandCompletion
        }

        try {
            commandHelper.registerCommandWithExecutor(linkCommandAliases, linkCommand, linkCommand)
        } catch (e: Exception) {
            RedeemXBot.instance.logger.warning("Command aliases are not yet supported on this server version.")
        }
    }
}