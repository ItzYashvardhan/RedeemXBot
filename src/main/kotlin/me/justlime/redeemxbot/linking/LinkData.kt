/*
 * RedeemCodeX - Plugin License Agreement
 * Copyright © 2024 Yashvardhan
 *
 * This software is a paid plugin developed by Yashvardhan ("Author") and is provided to you ("User") under the following terms:
 *
 * 1. Usage Rights:
 *    - This plugin is licensed, not sold.
 *    - One license grants usage on **one server network only**, unless explicitly agreed otherwise.
 *    - You may not sublicense, share, leak, or resell the plugin or any part of it.
 *
 * 2. Restrictions:
 *    - You may not decompile, reverse engineer, or modify the plugin.
 *    - You may not redistribute the plugin in any form.
 *    - You may not upload this plugin to any public or private repository or distribution platform.
 *
 * 3. Support & Updates:
 *    - Support is provided to verified buyers only.
 *    - Updates are available as long as development continues or within the support duration stated at purchase.
 *
 * 4. Termination:
 *    - Any violation of this agreement terminates your rights to use this plugin immediately, without refund.
 *
 * 5. No Warranty:
 *    - The plugin is provided "as is", without warranty of any kind. Use at your own risk.
 *    - The Author is not responsible for any damages, data loss, or server issues resulting from usage.
 *
 * For inquiries,
 * Email: itsyashvardhan76@gmail.com
 * Discord: https://discord.gg/rVsUJ4keZN
 */

package me.justlime.redeemxbot.linking

import java.util.*

/**
 * Represents the data stored for a pending link request.
 *
 * @param playerUUID The Minecraft player's unique ID.
 * @param code The temporary, unique code given to the player.
 * @param expiryTime The system timestamp (in milliseconds) when this code expires.
 */
data class PendingLink(
    val playerUUID: UUID,
    val code: String,
    val expiryTime: Long
)

/**
 * Represents a completed and stored account link.
 *
 * @param playerUUID The Minecraft player's unique ID.
 * @param playerName The Minecraft player's name (for display purposes).
 * @param discordId The Discord user's unique ID.
 */
data class AccountLink(
    val playerUUID: UUID,
    var playerName: String,
    val discordId: String
)
