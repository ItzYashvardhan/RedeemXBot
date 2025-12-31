package me.justlime.redeemxbot.listener

import me.justlime.redeemxbot.RedeemXBot
import me.justlime.redeemxbot.commands.DCommand
import me.justlime.redeemxbot.commands.PublicGenerateCommand
import me.justlime.redeemxbot.commands.redeemcode.RCXCreateCommand
import me.justlime.redeemxbot.commands.redeemcode.RCXDeleteCommand
import me.justlime.redeemxbot.commands.redeemcode.RCXUsageCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionContextType
import java.util.concurrent.ConcurrentHashMap

class DiscordCommandManager(private val jda: JDA, private val guilds: List<String>, private val roles: List<String>, private val channels: List<String>) : ListenerAdapter() {

    private val commands = ConcurrentHashMap<String, DCommand>()

    fun initializeCommands() {
        val guildCommands = listOf(
            RCXCreateCommand(),
            RCXDeleteCommand(),
            RCXUsageCommand(),
        )
        val globalCommands = listOf(
            PublicGenerateCommand()
        )
        jda.awaitReady()
        registerGuildCommands(guildCommands)
        registerGlobalCommands(globalCommands)
    }

    private fun registerGuildCommands(commandList: List<DCommand>) {
        commandList.forEach { cmd ->
            val data = cmd.buildCommand()
            commands[data.name] = cmd
        }
        val commandDataList = commandList.map { it.buildCommand() }

        jda.guilds.forEach { guild ->
            if (guild.id !in guilds) return@forEach
            guild.updateCommands().addCommands(commandDataList).queue { registered ->
                RedeemXBot.instance.logger.info("Registered ${registered.size} guild commands for ${guild.name}.")
            }
        }
    }

    private fun registerGlobalCommands(commandList: List<DCommand>) {
        commandList.forEach { cmd ->
            val data = cmd.buildCommand()
            commands[data.name] = cmd
        }
        val commandDataList = commandList.map { it.buildCommand() }

        jda.updateCommands().addCommands(commandDataList).queue { registered ->
            RedeemXBot.instance.logger.info("Registered ${registered.size} global commands.")
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = commands[event.name] ?: return

        // Handle DM commands (which are always public in this design)
        if (event.interaction.context == InteractionContextType.BOT_DM) {
            if (command is PublicGenerateCommand) {
                command.execute(event)
            }
            return
        }

        // Handle Guild commands
        if (event.isFromGuild) {
            val guildId = event.guild?.id ?: return
            if (guildId !in guilds) {
                event.guild?.leave()?.queue()
                return
            }

            // Public commands in a guild (if any were added) would be handled here
            if (command is PublicGenerateCommand) {
                command.execute(event)
                return
            }

            // --- Private Command Logic ---
            val member = event.member ?: return
            if (member.roles.none { it.id in roles }) {
                event.reply("You don't have permission to use this command.").setEphemeral(true).queue()
                return
            }

            if (event.channel.id !in channels) {
                event.reply("You can't use this command in this channel.").setEphemeral(true).queue()
                return
            }

            command.execute(event)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val command = commands[event.name] ?: return

        if (event.interaction.context == InteractionContextType.BOT_DM) {
            if (command is PublicGenerateCommand) {
                command.handleAutoComplete(event).let { event.replyChoices(it).queue() }
            }
            return
        }

        if (event.isFromGuild) {
            val guildId = event.guild?.id ?: return
            if (guildId !in guilds) return

            if (command is PublicGenerateCommand) {
                command.handleAutoComplete(event).let { event.replyChoices(it).queue() }
                return
            }

            // --- Private Command Logic ---
            if (event.channel.id !in channels) return
            val member = event.member ?: return
            if (member.roles.none { it.id in roles }) return

            command.handleAutoComplete(event).let { event.replyChoices(it).queue() }
        }
    }
}
