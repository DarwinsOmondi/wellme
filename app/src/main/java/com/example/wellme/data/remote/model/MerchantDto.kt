package com.example.wellme.data.remote.model

import com.example.wellme.domain.model.MerchantProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantDto(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("discount_tier") val discountTier: Double,
    @SerialName("pool_target_in_cents") val poolTargetInCents: Long,
    @SerialName("pool_raised_in_cents") val poolRaisedInCents: Long,
    @SerialName("is_verified") val isVerified: Boolean
) {
    fun toDomain() = MerchantProfile(
        merchantId = merchantId,
        businessName = businessName,
        discountTier = discountTier,
        poolTargetInCents = poolTargetInCents,
        poolRaisedInCents = poolRaisedInCents,
        isVerified = isVerified
    )

    companion object {
        fun fromDomain(domain: MerchantProfile) = MerchantDto(
            merchantId = domain.merchantId,
            businessName = domain.businessName,
            discountTier = domain.discountTier,
            poolTargetInCents = domain.poolTargetInCents,
            poolRaisedInCents = domain.poolRaisedInCents,
            isVerified = domain.isVerified
        )
    }
}
