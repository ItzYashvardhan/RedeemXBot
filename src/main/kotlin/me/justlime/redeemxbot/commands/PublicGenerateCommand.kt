package me.justlime.redeemxbot.commands

import me.justlime.redeemxbot.RedeemXBot
import me.justlime.redeemxbot.adapter.DiscordRCXSender
import me.justlime.redeemxbot.utils.JService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.justlime.redeemcodex.RedeemX
import org.bukkit.configuration.file.FileConfiguration
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class PublicGenerateCommand : DCommand {

    private val adLinkConfig: FileConfiguration by lazy {
        JService.adLink
    }

    // In-memory storage for user cooldowns. Maps UserId -> List of generation timestamps.
    private val userCooldowns = ConcurrentHashMap<String, MutableList<Long>>()

    override fun buildCommand(): CommandData {
        val commandName = adLinkConfig.getString("ad-link.command.name", "generate")!!
        val commandDescription = adLinkConfig.getString("ad-link.command.description", "Generate a unique link to claim a reward.")!!
        return Commands.slash(commandName, commandDescription)
            .setContexts(InteractionContextType.BOT_DM) // This makes the command available in DMs
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        if (!adLinkConfig.getBoolean("ad-link.enabled", false)) {
            event.reply("This feature is currently disabled.").setEphemeral(true).queue()
            return
        }

        // --- Cooldown Check ---
        val userId = event.user.id
        val currentTime = System.currentTimeMillis()
        val cooldownLimit = adLinkConfig.getInt("ad-link.cooldown.limit", 2)
        val cooldownDuration = adLinkConfig.getLong("ad-link.cooldown.duration-seconds", 3600) * 1000

        val userTimestamps = userCooldowns.computeIfAbsent(userId) { mutableListOf() }
        // Remove timestamps that are older than the cooldown duration
        userTimestamps.removeIf { it < currentTime - cooldownDuration }

        if (userTimestamps.size >= cooldownLimit) {
            val oldestTimestamp = userTimestamps.first()
            val remainingTime = (oldestTimestamp + cooldownDuration - currentTime) / 1000
            val cooldownMessage = adLinkConfig.getString("ad-link.cooldown-message", "You are on cooldown. Please try again in **{time}**.")
            event.reply(formatCooldownMessage(cooldownMessage ?: "You are on cooldown. Please try again in **{time}**.", remainingTime)).setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        // --- Template Selection ---
        val template = getWeightedRandomTemplate()
        if (template == null) {
            event.hook.sendMessage("Could not determine a reward template. Please contact an administrator.").queue()
            RedeemXBot.instance.logger.warning("No valid templates found in ad-link.yml or total chance is zero.")
            return
        }

        val adLinkFormat = adLinkConfig.getString("ad-link.ad-link-format")
        val redirectUrlFormat = adLinkConfig.getString("ad-link.redirect-url-format")
        val replyMessage = adLinkConfig.getString("ad-link.reply-message")

        if (adLinkFormat.isNullOrBlank() || redirectUrlFormat.isNullOrBlank() || replyMessage.isNullOrBlank()) {
            event.hook.sendMessage("Configuration for the generation link is incomplete. Please contact an administrator.").queue()
            RedeemXBot.instance.logger.warning("ad-link.yml is missing one or more required values!")
            return
        }

        // Use the uppercase version of the template name for the API call
        RedeemX.create.create(1, template.uppercase(), 1, emptyList(), DiscordRCXSender(event)) { generatedCodes ->
            val code = generatedCodes.firstOrNull()
            if (code == null) {
                event.hook.sendMessage("Could not generate a unique code at this time. Please try again later.").queue()
                return@create
            }

            // Add the current timestamp to the user's record *after* a code is successfully generated
            userTimestamps.add(currentTime)

            val finalUrl = redirectUrlFormat.replace("{code}", code.code)
            val encodedFinalUrl = URLEncoder.encode(finalUrl, StandardCharsets.UTF_8.toString())
            val adLink = adLinkFormat.replace("{redirect_url}", encodedFinalUrl)
            val finalMessage = replyMessage.replace("{link}", adLink)
            event.hook.sendMessage(finalMessage).queue()
        }
    }

    private fun getWeightedRandomTemplate(): String? {
        val templatesSection = adLinkConfig.getConfigurationSection("ad-link.templates") ?: return null
        
        val weightedList = templatesSection.getKeys(false).mapNotNull { key ->
            val chance = templatesSection.getDouble(key)
            if (chance > 0) key to chance else null
        }

        if (weightedList.isEmpty()) return null

        val totalWeight = weightedList.sumOf { it.second }
        if (totalWeight <= 0) return null

        var randomPoint = Random.nextDouble() * totalWeight
        for ((name, chance) in weightedList) {
            if (randomPoint < chance) return name
            randomPoint -= chance
        }

        return weightedList.lastOrNull()?.first // Fallback in case of floating point inaccuracies
    }

    private fun formatCooldownMessage(message: String, totalSeconds: Long): String {
        if (totalSeconds <= 0) return message.replace("{time}", "a few seconds")
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("$days day(s)")
        if (hours > 0) parts.add("$hours hour(s)")
        if (minutes > 0) parts.add("$minutes minute(s)")
        if (seconds > 0 || parts.isEmpty()) parts.add("$seconds second(s)")

        return message.replace("{time}", parts.joinToString(" "))
    }
}
