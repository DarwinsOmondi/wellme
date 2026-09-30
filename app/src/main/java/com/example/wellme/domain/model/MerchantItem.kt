package com.example.wellme.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MerchantItem(
    val id: String,
    val name: String,
    val price: Double,
    val stockUnits: Int,
    val category: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val merchantId: String
)
