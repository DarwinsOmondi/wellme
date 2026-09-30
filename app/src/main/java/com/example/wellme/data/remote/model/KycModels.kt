package com.example.wellme.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String?,
    val role: String
)

@Serializable
data class StudentKyc(
    val id: String,
    @SerialName("student_id_number") val studentIdNumber: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("campus_email") val campusEmail: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("student_id_photo_url") val studentIdPhotoUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class MerchantKyc(
    val id: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("national_id_number") val nationalIdNumber: String,
    @SerialName("national_id_doc_url") val nationalIdDocUrl: String? = null,
    @SerialName("registration_cert_url") val registrationCertUrl: String? = null,
    @SerialName("business_permit_url") val businessPermitUrl: String? = null,
    @SerialName("mpesa_till_doc_url") val mpesaTillDocUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
