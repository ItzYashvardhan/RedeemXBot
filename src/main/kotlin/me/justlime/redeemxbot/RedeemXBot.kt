package me.justlime.redeemxbot

import me.justlime.redeemxbot.commands.configuration.ConfigManager
import me.justlime.redeemxbot.listener.CommandManager
import me.justlime.redeemxbot.listener.GuildJoinListener
import me.justlime.redeemxbot.utils.JServices
import net.dv8tion.jda.api.JDA
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration


lateinit var rxbPlugin: RedeemXBot

class RedeemXBot : JavaPlugin() {
    private lateinit var jda: JDA

    override fun onEnable() {
        Class.forName("org.slf4j.LoggerFactory")

        setupConfig()
        rxbPlugin = this

        JServices.configManager = ConfigManager()

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) JServices.isPlaceholderHooked = true

        // Check if the bot is enabled
        if (!config.getBoolean("bot.enabled")) {
            logger.warning("Bot is disabled in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        // Retrieve bot token and guilds
        val token = config.getString("bot.token")?.takeIf { it.isNotEmpty() }
        val guilds = config.getStringList("guilds").takeIf { it.isNotEmpty() }
        val roles = config.getStringList("roles").takeIf { it.isNotEmpty() }
        val channels = config.getStringList("channels").takeIf { it.isNotEmpty() }

        if (token == null) {
            logger.severe("Bot token is missing in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        if (guilds == null) {
            logger.severe("Guilds are missing in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        if (roles == null) {
            logger.severe("Roles are missing in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        if (channels == null) {
            logger.severe("Channels are missing in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        // Initialize JDA
        try {
            try {
                jda = BotManager.buildBot(token).apply { awaitReady() }
            } catch (e: Exception) {
                this.logger.info("Force Reloaded Bot")
            }
            jda.guilds.forEach { guild ->
                if (guild.id !in guilds) {
                    logger.warning("Leaving unauthorized guild: ${guild.name} (${guild.id})")
                    guild.leave().queue()
                }
            }
            logger.info("Bot connected successfully.")

            // Initialize commands and register listeners
            val commandManager = CommandManager(jda, guilds, roles, channels)
            commandManager.initializeCommands()

            jda.addEventListener(commandManager)
            jda.addEventListener(GuildJoinListener(guilds))
            return
        } catch (exception: Exception) {
            logger.severe("Failed to initialize the Discord bot: ${exception.message}")
            exception.printStackTrace()
            return server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        if (::jda.isInitialized) {
            val client = jda.httpClient
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
            jda.shutdownNow()
            if (!jda.awaitShutdown(Duration.ofSeconds(3))) {
                jda.shutdownNow(); // Cancel request queue
                jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
            }
            logger.info("Bot has been shut down.")
        }
    }

    private fun setupConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        saveDefaultConfig()
    }
}
