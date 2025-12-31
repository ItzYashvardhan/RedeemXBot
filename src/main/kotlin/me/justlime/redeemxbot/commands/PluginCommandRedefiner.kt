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

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import java.lang.reflect.Constructor
import java.lang.reflect.Field

class PluginCommandRedefiner(private val plugin: Plugin) {

    fun registerCommand(aliases: List<String>): PluginCommand? {
        val command = getCommand(aliases[0]) ?: return null
        command.aliases = aliases.drop(1)
        getCommandMap()?.register(plugin.description.name, command)
        return command
    }

    fun registerCommandWithExecutor(aliases: List<String>, executor: CommandExecutor, tabCompleter: TabCompleter? = null): PluginCommand? {
        if (aliases.isEmpty()) return null
        val baseName = aliases[0]
        val command = getCommand(baseName) ?: return null

        command.setExecutor(executor)
        command.aliases = aliases.drop(1)
        if (tabCompleter != null) {
            command.tabCompleter = tabCompleter
        }

        getCommandMap()?.register(plugin.description.name, command)
        return command
    }

    fun unregisterCommand(vararg names: String) {
        try {
            val commandMap = getCommandMap() as? SimpleCommandMap ?: return
            val knownCommandsField = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
            knownCommandsField.isAccessible = true

            @Suppress("UNCHECKED_CAST") val knownCommands = knownCommandsField.get(commandMap) as MutableMap<String, Command>

            for (name in names) {
                val command = knownCommands[name] ?: continue
                command.aliases.forEach { alias -> knownCommands.remove(alias) }
                knownCommands.remove(command.name)
                command.unregister(commandMap)
            }

            knownCommandsField.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace() // Or use your plugin's logger
        }
    }

    private fun getCommand(name: String): PluginCommand? {
        return try {
            val constructor: Constructor<PluginCommand> = PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
            constructor.isAccessible = true
            constructor.newInstance(name, plugin)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCommandMap(): CommandMap? {
        return try {
            val pluginManager = Bukkit.getPluginManager()
            if (pluginManager is SimplePluginManager) {
                val field: Field = SimplePluginManager::class.java.getDeclaredField("commandMap")
                field.isAccessible = true
                field.get(pluginManager) as? CommandMap
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}