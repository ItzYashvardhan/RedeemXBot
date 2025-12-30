package me.justlime.redeemxbot.models

data class RazorPayProduct(
    val displayName: String,
    val amount: Int,
    val currency: String,
    val paymentLink: String
){
    companion object {
        fun fromMap(map: Map<String, Any>): RazorPayProduct {
            return RazorPayProduct(
                displayName = map["displayName"] as String,
                amount = map["amount"] as Int,
                currency = map["currency"] as String,
                paymentLink = map["payment-link"] as String
            )
        }
    }
}