package com.example.wellme.domain.usecase

import com.example.wellme.domain.repository.MerchantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMerchantPoolProgressUseCase(
    private val merchantRepository: MerchantRepository
) {
    operator fun invoke(merchantId: String): Flow<Float> {
        return merchantRepository.getMerchant(merchantId).map { merchant ->
            if (merchant == null || merchant.poolTargetInCents <= 0L) {
                0.0f
            } else {
                val ratio = merchant.poolRaisedInCents.toFloat() / merchant.poolTargetInCents.toFloat()
                ratio.coerceIn(0.0f, 1.0f)
            }
        }
    }
}
