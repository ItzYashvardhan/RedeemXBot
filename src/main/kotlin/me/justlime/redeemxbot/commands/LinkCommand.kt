package me.justlime.redeemxbot.commands

import me.justlime.redeemxbot.RedeemXBot
import me.justlime.redeemxbot.utils.JService
import net.justlime.redeemcodex.RedeemX
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class LinkCommand(private val plugin: RedeemXBot) : CommandExecutor, TabCompleter {

    private val linkManager get() = JService.linkManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String?>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command is only for players.")
            return true
        }

        // /link (Generate Code)
        if (args.isNullOrEmpty()) {
            handleGenerateCode(sender)
            return true
        }

        // 2. Subcommands
        val subCommand = args.getOrNull(0)?.lowercase() ?: return true

        when (subCommand) {
            "unlink" -> handleUnlink(sender)
            "status" -> handleStatus(sender)
            "notify" -> handleNotify(sender, args.getOrNull(1))
            "help" -> sendHelp(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleGenerateCode(player: Player) {
        val uuid = player.uniqueId

        if (linkManager.isPlayerLinked(uuid)) {
            val link = linkManager.getLinkByPlayer(uuid)
            player.sendMessage("${ChatColor.RED}You are already linked to Discord account ID: ${ChatColor.YELLOW}${link?.discordId}")
            player.sendMessage("${ChatColor.GRAY}Type ${ChatColor.WHITE}/link unlink${ChatColor.GRAY} to remove this link.")
            return
        }

        val pending = linkManager.generateCode(uuid)

        if (pending == null) {
            player.sendMessage("${ChatColor.RED}Unable to generate code. Are you already linked?")
            return
        }

        player.sendMessage("")
        player.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}Link Your Account")
        player.sendMessage("${ChatColor.GRAY}Your link code is: ${ChatColor.AQUA}${ChatColor.BOLD}${pending.code}")
        player.sendMessage("${ChatColor.GRAY}Please DM this code to the Discord Bot within 5 minutes.")
        player.sendMessage("")
    }

    private fun handleUnlink(player: Player) {
        val uuid = player.uniqueId

        if (!linkManager.isPlayerLinked(uuid)) {
            player.sendMessage("${ChatColor.RED}You are not linked to any account.")
            return
        }

        linkManager.unlink(uuid)
        player.sendMessage("${ChatColor.GREEN}Successfully unlinked your account.")
    }

    private fun handleStatus(player: Player) {
        val uuid = player.uniqueId
        val linked = linkManager.isPlayerLinked(uuid)
        val notify = linkManager.shouldNotify(uuid)

        player.sendMessage("")
        player.sendMessage("${ChatColor.GOLD}Link Status:")
        player.sendMessage("${ChatColor.GRAY}Linked: ${if (linked) "${ChatColor.GREEN}Yes" else "${ChatColor.RED}No"}")

        if (linked) {
            val link = linkManager.getLinkByPlayer(uuid)
            player.sendMessage("${ChatColor.GRAY}Discord ID: ${ChatColor.YELLOW}${link?.discordId}")
        }

        player.sendMessage("${ChatColor.GRAY}Notifications: ${if (notify) "${ChatColor.GREEN}ON" else "${ChatColor.RED}OFF"}")
        player.sendMessage("")
    }

    private fun handleNotify(player: Player, arg: String?) {
        if (arg == null) {
            RedeemX.notificationToggleDao.getStatus(player.uniqueId) { enabled ->
                linkManager.setNotificationStatus(player.uniqueId, !enabled)
                val statusStr = if (!enabled) "${ChatColor.GREEN}enabled" else "${ChatColor.RED}disabled"
                player.sendMessage("${ChatColor.YELLOW}Bot notifications have been $statusStr${ChatColor.YELLOW}.")
            }
            return
        }
        val enabled = when (arg.lowercase()) {
            "on", "true", "enable" -> true
            "off", "false", "disable" -> false
            else -> {
                player.sendMessage("${ChatColor.RED}Invalid option. Use 'on' or 'off'.")
                return
            }
        }

        linkManager.setNotificationStatus(player.uniqueId, enabled)
        val statusStr = if (enabled) "${ChatColor.GREEN}enabled" else "${ChatColor.RED}disabled"
        player.sendMessage("${ChatColor.YELLOW}Redeem notifications have been $statusStr${ChatColor.YELLOW}.")
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("${ChatColor.GOLD}RedeemX Linking Commands:")
        player.sendMessage("${ChatColor.YELLOW}/link ${ChatColor.GRAY}- Generate a code to link your Discord.")
        player.sendMessage("${ChatColor.YELLOW}/link status ${ChatColor.GRAY}- Check your link status.")
        player.sendMessage("${ChatColor.YELLOW}/link notify <on|off> ${ChatColor.GRAY}- Toggle reward notifications.")
        player.sendMessage("${ChatColor.YELLOW}/link unlink ${ChatColor.GRAY}- Unlink your account.")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String?>?): List<String>? {
        if (args.isNullOrEmpty()) return emptyList()

        if (args.size == 1) {
            val subCommands = listOf("unlink", "status", "notify", "help")
            return subCommands.filter { it.startsWith(args[0]!!, ignoreCase = true) }
        }

        if (args.size == 2 && args[0].equals("notify", ignoreCase = true)) {
            val options = listOf("on", "off")
            return options.filter { it.startsWith(args[1]!!, ignoreCase = true) }
        }

        return emptyList()
    }
}