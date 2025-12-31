package me.justlime.redeemxbot

import me.justlime.redeemxbot.commands.GameCommandManager
import me.justlime.redeemxbot.commands.configuration.ConfigManager
import me.justlime.redeemxbot.linking.LinkListener
import me.justlime.redeemxbot.linking.LinkManager
import me.justlime.redeemxbot.listener.DiscordCommandManager
import me.justlime.redeemxbot.listener.GuildJoinListener
import me.justlime.redeemxbot.utils.JService
import net.dv8tion.jda.api.JDA
import net.justlime.redeemcodex.RedeemX
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration

class RedeemXBot : JavaPlugin() {
    private lateinit var jda: JDA

    companion object{
        lateinit var instance: RedeemXBot
    }

    override fun onEnable() {
        Class.forName("org.slf4j.LoggerFactory")

        instance = this

        JService.configManager = ConfigManager()


        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            JService.isPlaceholderHooked = true
        }

        // Check if the bot is enabled
        if (!JService.config.getBoolean("bot.enabled")) {
            logger.warning("Bot is disabled in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }

        // Retrieve bot token and guilds
        val token = JService.config.getString("bot.token")?.takeIf { it.isNotEmpty() }
        val guilds = JService.config.getStringList("guilds").takeIf { it.isNotEmpty() }
        val roles = JService.config.getStringList("roles").takeIf { it.isNotEmpty() }
        val channels = JService.config.getStringList("channels").takeIf { it.isNotEmpty() }

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
            val discordCommandManager = DiscordCommandManager(jda, guilds, roles, channels)
            discordCommandManager.initializeCommands()

            JService.linkManager = LinkManager(RedeemX.accountLinkDao, RedeemX.notificationToggleDao)
            JService.linkManager.loadCache()
            GameCommandManager(this)

            jda.addEventListener(discordCommandManager)
            jda.addEventListener(GuildJoinListener(guilds))
            jda.addEventListener(LinkListener(JService.linkManager)) // Add the DM listener

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

    fun getJDA(): JDA = jda
}
