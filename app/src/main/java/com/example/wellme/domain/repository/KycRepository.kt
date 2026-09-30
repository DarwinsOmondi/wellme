package com.example.wellme.domain.repository

import com.example.wellme.data.remote.model.MerchantKyc
import com.example.wellme.data.remote.model.StudentKyc

interface KycRepository {
    suspend fun uploadStudentKyc(
        studentIdNumber: String,
        studentName: String,
        campusEmail: String,
        phoneNumber: String,
        idPhotoBytes: ByteArray,
        idPhotoExtension: String
    ): Result<Unit>

    suspend fun uploadMerchantKyc(
        businessName: String,
        nationalIdNumber: String,
        nationalIdDoc: Pair<ByteArray, String>?,
        registrationCert: Pair<ByteArray, String>?,
        businessPermit: Pair<ByteArray, String>?,
        mpesaTillDoc: Pair<ByteArray, String>?
    ): Result<Unit>

    suspend fun getStudentKyc(userId: String): Result<StudentKyc?>
    suspend fun getMerchantKyc(userId: String): Result<MerchantKyc?>
    suspend fun updateAvatar(imageBytes: ByteArray, extension: String): Result<String>
}
