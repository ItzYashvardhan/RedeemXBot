package me.justlime.redeemxbot

import me.justlime.redeemxbot.commands.configuration.ConfigManager
import me.justlime.redeemxbot.linking.LinkCommand
import me.justlime.redeemxbot.linking.LinkListener
import me.justlime.redeemxbot.linking.LinkManager
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
    private lateinit var linkManager: LinkManager

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
            jda = BotManager.buildBot(token).apply { awaitReady() }
            logger.info("Bot connected successfully.")

            // Initialize commands and register listeners
            val commandManager = CommandManager(jda, guilds, roles, channels)
            commandManager.initializeCommands()

            //Initialize Account Linking System
            linkManager = LinkManager()
            linkManager.loadLinks()
            val linkCommandName = linkManager.config.getString("linking.command-name", "linkdiscord")!!
            val linkCommandAliases = linkManager.config.getStringList("linking.command-aliases")
            getCommand(linkCommandName)?.let {
                it.setExecutor(LinkCommand(linkManager))
                it.aliases = linkCommandAliases
            }

            jda.addEventListener(commandManager)
            jda.addEventListener(GuildJoinListener(guilds))
            jda.addEventListener(LinkListener(linkManager)) // Add the DM listener

        } catch (exception: Exception) {
            logger.severe("Failed to initialize the Discord bot: ${exception.message}")
            exception.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        if (::jda.isInitialized) {
            val client = jda.httpClient
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
            jda.shutdownNow()
            if (!jda.awaitShutdown(Duration.ofSeconds(3))) {
                jda.shutdownNow()
                jda.awaitShutdown()
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

    fun getJDA(): JDA = jda
}
