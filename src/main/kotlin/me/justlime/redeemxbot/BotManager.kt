package me.justlime.redeemxbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder

object BotManager {
    private lateinit var jda: JDA
    fun buildBot(token: String): JDA {
        jda = JDABuilder.createDefault(token).build()
        return jda
    }

}
