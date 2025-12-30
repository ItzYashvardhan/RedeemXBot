package me.justlime.redeemxbot.listener

import me.justlime.redeemxbot.commands.JRedeemCode
import me.justlime.redeemxbot.commands.redeemcode.RCXCreateCommand
import me.justlime.redeemxbot.commands.redeemcode.RCXDeleteCommand
import me.justlime.redeemxbot.commands.redeemcode.RCXUsageCommand
import me.justlime.redeemxbot.rxbPlugin
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandManager(
    private val jda: JDA,
    private val guilds: List<String>,
    private val roles: List<String>,
    private val channels: List<String>
) : ListenerAdapter() {

    private val commands = mutableMapOf<String, JRedeemCode>()

    fun initializeCommands() {
        val commands = listOf(
            RCXCreateCommand(),
            RCXDeleteCommand(),
//            RCXModifyCommand(),
            RCXUsageCommand(),
        )
        jda.awaitReady()
        register(*commands.toTypedArray())
    }

    /**
     * Registers all commands at once.
     */
    private fun register(vararg commandList: JRedeemCode) {
        // Register commands to the internal map
        commandList.forEach { cmd ->
            val data = cmd.buildCommand()
            commands[data.name] = cmd
        }

        // Prepare a list of all command data
        val commandDataList = commands.values.map { it.buildCommand() }

        // Register to all guilds
        jda.guilds.forEach { guild ->
            guild.updateCommands()
                .addCommands(commandDataList)
                .queue { registered ->
                    // log registered command names
                     rxbPlugin.logger.info("Registered commands for ${guild.name}: ${registered.map { it.name }}")
                }
        }
    }


    /**
     * Handles command execution.
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return
        val guildId = event.guild?.id ?: return

        if (!guilds.contains(guildId)) {
            event.guild?.leave()?.queue()
            return
        }

        val member = event.member ?: return
        if (!member.roles.any { it.id in roles }) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue()
            return
        }

        if (event.channel.id !in channels) {
            event.reply("You can't use this command in this channel.").setEphemeral(true).queue()
            return
        }

        commands[event.name]?.execute(event)
            ?: event.reply("Unknown command!").setEphemeral(true).queue()
    }

    /**
     * Handles auto-completion events.
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!event.isFromGuild) return
        val guildId = event.guild?.id ?: return
        if (!guilds.contains(guildId)) {
            event.guild?.leave()?.queue()
            return
        }

        if (event.channel.id !in channels) return

        val member = event.member ?: return
        if (!member.roles.any { it.id in roles }) return

        val command = commands[event.name]
        if (command != null) {
            val completions = command.handleAutoComplete(event)
            event.replyChoices(completions).queue()
        }
    }
}
