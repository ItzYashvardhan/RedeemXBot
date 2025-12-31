package me.justlime.redeemxbot.utils

import me.justlime.redeemxbot.RedeemXBot
import net.justlime.redeemcodex.RedeemX
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.logging.Level

object ConfigLoader {

    private val corePlugin: JavaPlugin = RedeemX.plugin
    private val botPlugin: RedeemXBot = RedeemXBot.instance

    fun loadConfig(directory: String, fileName: String): FileConfiguration {
        val targetDir = File(corePlugin.dataFolder, directory)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val targetFile = File(targetDir, fileName)
        val resourcePath = "$directory/$fileName"

        if (!targetFile.exists()) {
            val botResource = botPlugin.getResource(resourcePath)

            if (botResource != null) {
                saveResourceToExternalFolder(targetFile, botResource)
            } else {
                botPlugin.logger.warning("Could not find default resource for: $resourcePath in Bot JAR.")
            }
        }

        return YamlConfiguration.loadConfiguration(targetFile)
    }

    private fun saveResourceToExternalFolder(outFile: File, inputStream: InputStream) {
        try {
            if (!outFile.parentFile.exists()) {
                outFile.parentFile.mkdirs()
            }

            inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (ex: IOException) {
            botPlugin.logger.log(Level.SEVERE, "Could not save ${outFile.name} to $outFile", ex)
        }
    }
}