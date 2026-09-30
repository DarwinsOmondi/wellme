package com.example.wellme.data.remote.model

import com.example.wellme.domain.model.MerchantItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("price") val price: Double,
    @SerialName("stock_units") val stockUnits: Int,
    @SerialName("category") val category: String,
    @SerialName("description") val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("merchant_id") val merchantId: String
) {
    fun toDomain() = MerchantItem(
        id = id ?: "",
        name = name,
        price = price,
        stockUnits = stockUnits,
        category = category,
        description = description,
        imageUrl = imageUrl,
        merchantId = merchantId
    )

    companion object {
        fun fromDomain(domain: MerchantItem) = MerchantItemDto(
            id = domain.id.ifBlank { null },
            name = domain.name,
            price = domain.price,
            stockUnits = domain.stockUnits,
            category = domain.category,
            description = domain.description,
            imageUrl = domain.imageUrl,
            merchantId = domain.merchantId
        )
    }
}
