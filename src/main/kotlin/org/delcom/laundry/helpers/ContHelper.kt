package org.delcom.laundry.helpers

object JWTConstants {
    const val NAME     = "auth-jwt"
    const val REALM    = "laundry-realm"
    const val ISSUER   = "laundry-app"
    const val AUDIENCE = "laundry-user"
}

object ServiceTypes {
    val ALLOWED = setOf("Wash & Fold", "Dry Cleaning", "Iron Only", "Wash & Iron")

    val PRICE_PER_KG = mapOf(
        "Wash & Fold"  to 15000L,
        "Dry Cleaning" to 20000L,
        "Iron Only"    to 8000L,
        "Wash & Iron"  to 18000L,
    )

    fun calculateCost(serviceType: String, weightKg: Double): Long =
        ((PRICE_PER_KG[serviceType] ?: 15000L) * weightKg).toLong()
}

object OrderStatuses {
    val ALLOWED = setOf("New", "Washing", "In Progress", "Ready for Pickup", "Completed")
}
