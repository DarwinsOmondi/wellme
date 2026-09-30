package com.example.wellme.data.remote.model

import com.example.wellme.domain.model.StudentWallet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentWalletDto(
    @SerialName("student_id") val studentId: String,
    @SerialName("balance_in_cents") val balanceInCents: Long,
    @SerialName("status") val status: String
) {
    fun toDomain() = StudentWallet(
        studentId = studentId,
        balanceInCents = balanceInCents,
        status = status
    )

    companion object {
        fun fromDomain(domain: StudentWallet) = StudentWalletDto(
            studentId = domain.studentId,
            balanceInCents = domain.balanceInCents,
            status = domain.status
        )
    }
}
