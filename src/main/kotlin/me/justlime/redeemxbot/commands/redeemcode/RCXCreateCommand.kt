package me.justlime.redeemxbot.commands.redeemcode

import me.justlime.redeemxbot.adapter.DiscordRCXSender
import me.justlime.redeemxbot.commands.DCommand
import me.justlime.redeemxbot.enums.JMessages
import me.justlime.redeemxbot.utils.JService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.justlime.redeemcodex.RedeemX

class RCXCreateCommand : DCommand {

    companion object {
        private const val MAX_DIGITS: Long = 25
        private const val MAX_AMOUNT: Long = 100
        private const val DEFAULT_TEMPLATE = "DEFAULT"
    }

    override fun buildCommand(): CommandData {
        return Commands.slash(
            JService.getCommandString(JMessages.GENERATE_COMMAND.path),
            JService.getCommandString(JMessages.GENERATE_DESCRIPTION.path)
        ).addOptions(
            OptionData(
                OptionType.INTEGER,
                JService.getCommandString(JMessages.GENERATE_DIGIT_COMPLETION.path),
                JService.getCommandString(JMessages.GENERATE_DIGIT_DESCRIPTION.path),
                false
            ).setMinValue(1).setMaxValue(MAX_DIGITS),
            OptionData(
                OptionType.STRING,
                JService.getCommandString(JMessages.GENERATE_CUSTOM_COMPLETION.path),
                JService.getCommandString(JMessages.GENERATE_CUSTOM_DESCRIPTION.path),
                false
            ),
            OptionData(
                OptionType.INTEGER,
                JService.getCommandString(JMessages.GENERATE_AMOUNT_COMPLETION.path),
                JService.getCommandString(JMessages.GENERATE_AMOUNT_DESCRIPTION.path),
                false
            ).setMinValue(1).setMaxValue(MAX_AMOUNT),
            OptionData(
                OptionType.STRING,
                JService.getCommandString(JMessages.GENERATE_TEMPLATE_COMPLETION.path),
                JService.getCommandString(JMessages.GENERATE_TEMPLATE_DESCRIPTION.path),
                false
            ).setAutoComplete(true)
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val digit = event.getOption(JService.getCommandString(JMessages.GENERATE_DIGIT_COMPLETION.path))?.asInt
        val customCodesRaw = event.getOption(JService.getCommandString(JMessages.GENERATE_CUSTOM_COMPLETION.path))?.asString
        val amount = event.getOption(JService.getCommandString(JMessages.GENERATE_AMOUNT_COMPLETION.path))?.asInt ?: 1
        val template = event.getOption(JService.getCommandString(JMessages.GENERATE_TEMPLATE_COMPLETION.path))?.asString ?: DEFAULT_TEMPLATE

        // 1. Defer Reply: Essential because database ops take time
        event.deferReply().queue()

        // 2. Create the Adapter: Wraps the Discord event so it looks like a "Sender" to the service
        val discordSender = DiscordRCXSender(event)

        val customCodesList = customCodesRaw?.split(" ") ?: emptyList()

        // CASE 1: Random Generation (With or without custom codes included)
        if (digit != null) {
            // Pass 'discordSender' instead of 'null'
            // The Service will automatically call discordSender.sendMessage(...) using the message from messages.yml
            RedeemX.create.create(digit, template, amount, customCodesList, discordSender) { _ ->
                // Callback is now empty because the Service handles the success/failure message!
            }
            return
        }

        // CASE 2: Only Custom Codes (No random digits specified)
        if (customCodesList.isNotEmpty()) {
            RedeemX.create.create(customCodesList, template, discordSender) { _ ->
                // Logic handled by Service + Adapter
            }
            return
        }

        // CASE 3: No inputs provided (Fallback)
        event.hook.sendMessage("Please provide either a digit for random generation or custom codes.").setEphemeral(true).queue()
    }


    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        return when (focusedOption) {
            JService.getCommandString(JMessages.GENERATE_TEMPLATE_COMPLETION.path) -> {
                RedeemX.redeemTemplateDao.getTemplates()
                    .take(25)
                    .map { Command.Choice(it, it) }
            }
            else -> emptyList()
        }
    }
}