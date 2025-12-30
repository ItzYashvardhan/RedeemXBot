package me.justlime.redeemxbot.commands.redeemcode

import me.justlime.redeemxbot.commands.JRedeemCode
import me.justlime.redeemxbot.enums.JMessages
import me.justlime.redeemxbot.utils.JServices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class RCXUsageCommand : JRedeemCode {
    override fun buildCommand(): CommandData {
        return Commands.slash(
            JServices.getMessage(JMessages.USAGE_COMMAND.path),
            JServices.getMessage(JMessages.USAGE_DESCRIPTION.path)
        ).addSubcommands(
            SubcommandData(
                JServices.getMessage(JMessages.USAGE_CODE_SUBCOMMAND.path),
                JServices.getMessage(JMessages.USAGE_CODE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JServices.getMessage(JMessages.USAGE_CODE_COMPLETION.path),
                    JServices.getMessage(JMessages.USAGE_CODE_OPTION_DESCRIPTION.path),
                    true
                ).setAutoComplete(true)
            ),
            SubcommandData(
                JServices.getMessage(JMessages.USAGE_TEMPLATE_SUBCOMMAND.path),
                JServices.getMessage(JMessages.USAGE_TEMPLATE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JServices.getMessage(JMessages.USAGE_TEMPLATE_COMPLETION.path),
                    JServices.getMessage(JMessages.USAGE_TEMPLATE_OPTION_DESCRIPTION.path),
                    true
                ).setAutoComplete(true)
            )
        ).setContexts(InteractionContextType.GUILD)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val subcommand = event.subcommandName
        val code = event.getOption(JServices.getMessage(JMessages.USAGE_CODE_COMPLETION.path))?.asString
        val template = event.getOption(JServices.getMessage(JMessages.USAGE_TEMPLATE_COMPLETION.path))?.asString

//        val reply = when (subcommand) {
//            JServices.getMessage(JMessages.USAGE_CODE_SUBCOMMAND.path) -> {
//
//                val redeemCode = RedeemXAPI.code.getCode(code ?: "") ?: return event.reply("Invalid code.").setEphemeral(true).queue()
//                val placeHolder = RedeemXAPI.code.getRCXPlaceHolder(redeemCode)
//                placeHolder.totalPlayerUsage = redeemCode.usedBy.size.toString()
//                var message = ""
//                message += JServices.getMessage(JMessages.USAGES_CODE_ENABLED.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_TEMPLATE.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_SYNC.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REDEMPTION.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_LIMIT.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_PIN.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_PERMISSION.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_DURATION.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_COOLDOWN.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REWARD_TITLE.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REWARD_SUBTITLE.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REWARD_ACTIONBAR.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REWARD_SOUND.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_TARGET_LIST.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_REWARD_MESSAGE.path)
//                message += JServices.getMessage(JMessages.USAGES_CODE_COMMANDS.path)
//                JServices.applyPlaceholders(message, placeHolder)
//            }
//
//            JServices.getMessage(JMessages.USAGE_TEMPLATE_SUBCOMMAND.path) -> {
//                val redeemTemplate = try {
//                    RedeemXAPI.template.getTemplate(template ?: "") ?: return event.reply(JServices.getMessage(JMessages.INVALID_TEMPLATE.path)).setEphemeral(true).queue()
//                } catch (e: Exception) {
//                    return event.reply(JServices.getMessage(JMessages.INVALID_TEMPLATE.path)).setEphemeral(true).queue()
//                }
//                val placeHolder = RedeemXAPI.template.getRCXPlaceHolder(redeemTemplate)
//                var message = ""
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_ENABLED.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_SYNC.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REDEMPTION.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_LIMIT.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_PIN.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_PERMISSION.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_DURATION.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_COOLDOWN.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REWARD_TITLE.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REWARD_SUBTITLE.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REWARD_ACTIONBAR.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REWARD_SOUND.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_REWARD_MESSAGE.path)
//                message += JServices.getMessage(JMessages.USAGES_TEMPLATE_COMMANDS.path)
//                JServices.applyPlaceholders(message, placeHolder)
//
//            }
//
//            else -> JServices.getMessage(JMessages.INVALID_SUBCOMMAND.path)
//        }

    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val query = event.focusedOption.value.lowercase()
        val maxChoices = 25

//        return when (focusedOption) {
//            JServices.getMessage(JMessages.USAGE_CODE_COMPLETION.path) -> {
//                RedeemXAPI.code.getCodes()
//                    .filter { it.lowercase().contains(query) }
//                    .take(maxChoices)
//                    .map { Command.Choice(it, it) }
//            }
//
//            JServices.getMessage(JMessages.USAGE_TEMPLATE_COMPLETION.path) -> {
//                RedeemXAPI.template.getTemplates()
//                    .filter { it.lowercase().contains(query) }
//                    .take(maxChoices)
//                    .map { Command.Choice(it, it) }
//            }
//
//            else -> emptyList()
//        }
        return emptyList()
    }
}
