package me.justlime.redeemxbot.commands.redeemcode

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

class RCXModifyCommand {
    fun buildCommand(): CommandData {
        return Commands.slash("modify", "Modify a redeem code or template").addSubcommands(
            // Subcommand for modifying a code
            SubcommandData("code", "Modify a redeem code").addOptions(
                OptionData(
                    OptionType.STRING,
                    "code",
                    "The code to modify",
                    true
                ).setAutoComplete(true), // Enable autocomplete for codes
                OptionData(OptionType.STRING, "property", "The property to modify", true).setAutoComplete(true),
                OptionData(OptionType.STRING, "value", "The new value for the property", false),
            ),
            // Subcommand for modifying a template
            SubcommandData("template", "Modify a template").addOptions(
                OptionData(
                    OptionType.STRING, "template", "The template to modify", true
                ).setAutoComplete(true), // Enable autocomplete for templates
                OptionData(OptionType.STRING, "property", "The property to modify", true).setAutoComplete(true),
                OptionData(OptionType.STRING, "value", "The new value for the property", false),
            )
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    fun execute(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString
        val template = event.getOption("template")?.asString
        val type = if (code != null) "code" else "template"
        val property = event.getOption("property")?.asString ?: return
        val value = event.getOption("value")?.asString


        when (type) {
            "code" -> {}
            "template" -> {}
        }

        val message = ""
        event.reply("```\n$message\n```").queue()

    }

    fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val property = mutableListOf<String>()
        val query = event.focusedOption.value.lowercase() // User's input for filtering
        val maxChoices = 25 // Discord's limit for choices
        return when (focusedOption) {
            "code" -> {
                val availableCodes = RedeemX.redeemCodeDao.getCachedCodes()
                availableCodes.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            "template" -> {
                val availableTemplates = RedeemX.redeemTemplateDao.getTemplates()
                availableTemplates.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            "property" -> property.filter { it.lowercase().contains(query) }.take(maxChoices)
                .map { Command.Choice(it, it) }

            "value" -> emptyList()
            else -> emptyList()
        }
    }
}