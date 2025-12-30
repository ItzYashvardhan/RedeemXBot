package me.justlime.redeemxbot.commands.configuration

import me.justlime.redeemxbot.enums.JFiles
import me.justlime.redeemxbot.enums.JMessages
import me.justlime.redeemxbot.rxbPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager {
    lateinit var configuration: FileConfiguration
    lateinit var messages: FileConfiguration
    lateinit var payments: FileConfiguration


    init {
        loadMessageConfig()
    }

    fun loadMessageConfig() {
        this.configuration = rxbPlugin.config
        val file = getFile(JFiles.MESSAGES.fileName)
        val config = YamlConfiguration.loadConfiguration(file)
        JMessages.entries.forEach {
            if (config.getString(it.path) == null) {
                config.set(it.path, it.path)
            }
        }
        config.save(file)
        this.messages = config
        this.payments = YamlConfiguration.loadConfiguration(getFile(JFiles.PAYMENTS.fileName))

    }

    private fun getFile(jFiles: String): File {
        val file = File(rxbPlugin.dataFolder.path, jFiles)
        if (!file.exists()) {
            rxbPlugin.saveResource(jFiles, false)
        }
        return file
    }

    fun reload(){
        loadMessageConfig()
    }


}