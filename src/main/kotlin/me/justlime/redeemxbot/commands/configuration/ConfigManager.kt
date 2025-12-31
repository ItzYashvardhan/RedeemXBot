package me.justlime.redeemxbot.commands.configuration

import me.justlime.redeemxbot.enums.JMessages
import me.justlime.redeemxbot.utils.ConfigLoader
import me.justlime.redeemxbot.utils.JService
import org.bukkit.configuration.file.FileConfiguration

class ConfigManager {
    lateinit var configuration: FileConfiguration

    init {
        reload()
    }

    fun reload() {

        // Load the commands configuration from the 'bot' subdirectory
        JService.config = ConfigLoader.loadConfig("bot", "config.yml")
        JService.commands = ConfigLoader.loadConfig("bot", "commands.yml")
        JService.messages = ConfigLoader.loadConfig("bot", "messages.yml")
        JService.adLink = ConfigLoader.loadConfig("bot", "ad-link.yml")
        JService.linking = ConfigLoader.loadConfig("bot", "linking.yml")


        // Ensure default values are present
        JMessages.entries.forEach {
            if (JService.commands.getString(it.path) == null) {
                JService.commands.set(it.path, it.path)
            }
        }
    }
}
