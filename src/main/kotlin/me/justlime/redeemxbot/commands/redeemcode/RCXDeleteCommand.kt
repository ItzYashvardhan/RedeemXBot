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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.justlime.redeemcodex.RedeemX

class RCXDeleteCommand : DCommand {

    override fun buildCommand(): CommandData {
        return Commands.slash(
            JService.getCommandString(JMessages.DELETE_COMMAND.path),
            JService.getCommandString(JMessages.DELETE_DESCRIPTION.path)
        ).addSubcommands(
            SubcommandData(
                JService.getCommandString(JMessages.DELETE_CODE_SUBCOMMAND.path),
                JService.getCommandString(JMessages.DELETE_CODE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JService.getCommandString(JMessages.DELETE_CODE_COMPLETION.path),
                    JService.getCommandString(JMessages.DELETE_CODE_OPTION_DESCRIPTION.path),
                    false
                ).setAutoComplete(true)
            ),
            SubcommandData(
                JService.getCommandString(JMessages.DELETE_TEMPLATE_SUBCOMMAND.path),
                JService.getCommandString(JMessages.DELETE_TEMPLATE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JService.getCommandString(JMessages.DELETE_TEMPLATE_COMPLETION.path),
                    JService.getCommandString(JMessages.DELETE_TEMPLATE_OPTION_DESCRIPTION.path),
                    false
                ).setAutoComplete(true)
            )
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val subcommand = event.subcommandName ?: return

        // 1. Defer Reply
        event.deferReply().queue()

        // 2. Create the Adapter
        val discordSender = DiscordRCXSender(event)

        when (subcommand) {
            JService.getCommandString(JMessages.DELETE_CODE_SUBCOMMAND.path) -> {
                val input = event.getOption(JService.getCommandString(JMessages.DELETE_CODE_COMPLETION.path))?.asString ?: ""
                val codes = input.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

                if (codes.isEmpty()) {
                    event.hook.sendMessage("No codes provided.").setEphemeral(true).queue()
                    return
                }

                // 3. Call Async Service with Adapter
                // The Service will automatically invoke discordSender.sendMessage() with the correct config message.
                RedeemX.delete.deleteCodes(discordSender, codes) { remainingCodes ->
                    // Callback is empty because feedback is handled by the Service/Adapter now.
                    // You can add specific extra logging here if needed, but standard logs are also in the Service.
                }
            }

            JService.getCommandString(JMessages.DELETE_TEMPLATE_SUBCOMMAND.path) -> {
                val input = event.getOption(JService.getCommandString(JMessages.DELETE_TEMPLATE_COMPLETION.path))?.asString ?: ""
                val templates = input.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

                if (templates.isEmpty()) {
                    event.hook.sendMessage("No templates provided.").setEphemeral(true).queue()
                    return
                }

                // 3. Call Async Service with Adapter
                RedeemX.delete.deleteTemplates(discordSender, templates, true) { remainingTemplates ->
                    // Feedback handled by Service/Adapter.
                }
            }
        }
    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val fullInput = event.focusedOption.value
        val endsWithSpace = fullInput.endsWith(" ")
        val parts = fullInput.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

        val alreadyEntered = parts.dropLast(if (endsWithSpace) 0 else 1).toSet()
        val query = if (endsWithSpace) "" else parts.lastOrNull()?.lowercase() ?: ""
        val prefix = if (endsWithSpace) fullInput.trim() else parts.dropLast(1).joinToString(" ")
        val maxChoices = 25

        return when (focusedOption) {
            JService.getCommandString(JMessages.DELETE_CODE_COMPLETION.path) -> {
                RedeemX.redeemCodeDao.getCachedCodes()
                    .filter { code ->
                        val lower = code.lowercase()
                        lower.contains(query) && !alreadyEntered.contains(code)
                    }
                    .take(maxChoices)
                    .map { code ->
                        val suggestion = if (prefix.isNotEmpty()) "$prefix $code" else code
                        Command.Choice(suggestion, suggestion)
                    }
            }

            JService.getCommandString(JMessages.DELETE_TEMPLATE_COMPLETION.path) -> {
                RedeemX.redeemTemplateDao.getTemplates()
                    .filter { template ->
                        val lower = template.lowercase()
                        lower.contains(query) && !alreadyEntered.contains(template)
                    }
                    .take(maxChoices)
                    .map { template ->
                        val suggestion = if (prefix.isNotEmpty()) "$prefix $template" else template
                        Command.Choice(suggestion, suggestion)
                    }
            }

            else -> emptyList()
        }
    }
}