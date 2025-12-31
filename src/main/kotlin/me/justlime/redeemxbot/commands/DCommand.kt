package me.justlime.redeemxbot.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface DCommand {
    fun buildCommand(): CommandData
    fun execute(event: SlashCommandInteractionEvent)
    fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> = emptyList()
}