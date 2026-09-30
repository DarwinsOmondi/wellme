package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.model.MerchantKyc
import com.example.wellme.data.remote.model.StudentKyc
import com.example.wellme.domain.repository.KycRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class KycRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage
) : KycRepository {

    private val TAG = "KycRepositoryImpl"

    override suspend fun uploadStudentKyc(
        studentIdNumber: String,
        studentName: String,
        campusEmail: String,
        phoneNumber: String,
        idPhotoBytes: ByteArray,
        idPhotoExtension: String
    ): Result<Unit> {
        val userId = auth.currentUserOrNull()?.id ?: run {
            Log.e(TAG, "uploadStudentKyc failed: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }
        
        Log.d(TAG, "Starting student KYC upload for userId: $userId")
        
        return try {
            // 1. Upload to Storage
            val path = "$userId/student_id.$idPhotoExtension"
            Log.d(TAG, "Uploading student ID photo to: $path")
            val bucket = storage.from("student-docs")
            bucket.upload(path, idPhotoBytes) {
                upsert = true
            }
            
            // 2. Get Public URL (or just save path)
            val publicUrl = bucket.publicUrl(path)
            Log.d(TAG, "Student ID photo uploaded. Public URL: $publicUrl")

            // 3. Save to database
            val kyc = StudentKyc(
                id = userId,
                studentIdNumber = studentIdNumber,
                studentName = studentName,
                campusEmail = campusEmail,
                phoneNumber = phoneNumber,
                studentIdPhotoUrl = publicUrl
            )
            Log.d(TAG, "Inserting student KYC record into database")
            postgrest.from("student_kyc").insert(kyc)

            // 4. Initialize Student Wallet
            Log.d(TAG, "Initializing student wallet for: $userId")
            postgrest.from("student_wallets").insert(
                buildJsonObject {
                    put("student_id", userId)
                    put("balance_in_cents", 0)
                    put("status", "ACTIVE")
                }
            )
            
            Log.d(TAG, "Student KYC and Wallet setup successful for: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Student KYC upload failed for: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadMerchantKyc(
        businessName: String,
        nationalIdNumber: String,
        nationalIdDoc: Pair<ByteArray, String>?,
        registrationCert: Pair<ByteArray, String>?,
        businessPermit: Pair<ByteArray, String>?,
        mpesaTillDoc: Pair<ByteArray, String>?
    ): Result<Unit> {
        val userId = auth.currentUserOrNull()?.id ?: run {
            Log.e(TAG, "uploadMerchantKyc failed: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }
        
        Log.d(TAG, "Starting merchant KYC upload for userId: $userId")
        val bucket = storage.from("merchant-docs")

        return try {
            val nationalIdUrl = nationalIdDoc?.let { (bytes, ext) ->
                val path = "$userId/national_id.$ext"
                Log.d(TAG, "Uploading national ID to: $path")
                bucket.upload(path, bytes) { upsert = true }
                bucket.publicUrl(path)
            }
            
            val registrationCertUrl = registrationCert?.let { (bytes, ext) ->
                val path = "$userId/registration_cert.$ext"
                Log.d(TAG, "Uploading registration cert to: $path")
                bucket.upload(path, bytes) { upsert = true }
                bucket.publicUrl(path)
            }
            
            val businessPermitUrl = businessPermit?.let { (bytes, ext) ->
                val path = "$userId/business_permit.$ext"
                Log.d(TAG, "Uploading business permit to: $path")
                bucket.upload(path, bytes) { upsert = true }
                bucket.publicUrl(path)
            }
            
            val mpesaTillDocUrl = mpesaTillDoc?.let { (bytes, ext) ->
                val path = "$userId/mpesa_till.$ext"
                Log.d(TAG, "Uploading M-Pesa till doc to: $path")
                bucket.upload(path, bytes) { upsert = true }
                bucket.publicUrl(path)
            }

            val kyc = MerchantKyc(
                id = userId,
                businessName = businessName,
                nationalIdNumber = nationalIdNumber,
                nationalIdDocUrl = nationalIdUrl,
                registrationCertUrl = registrationCertUrl,
                businessPermitUrl = businessPermitUrl,
                mpesaTillDocUrl = mpesaTillDocUrl
            )
            
            Log.d(TAG, "Inserting merchant KYC record into database")
            postgrest.from("merchant_kyc").insert(kyc)

            // 4. Initialize Merchant Profile
            Log.d(TAG, "Initializing merchant profile for: $userId")
            postgrest.from("merchants").insert(
                buildJsonObject {
                    put("merchant_id", userId)
                    put("business_name", businessName)
                    put("discount_tier", 0.15)
                    put("pool_target_in_cents", 0)
                    put("pool_raised_in_cents", 0)
                    put("is_verified", false)
                }
            )
            
            Log.d(TAG, "Merchant KYC and Profile setup successful for: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Merchant KYC upload failed for: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun getStudentKyc(userId: String): Result<StudentKyc?> {
        Log.d(TAG, "Fetching student KYC for: $userId")
        return try {
            val kyc = postgrest.from("student_kyc")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<StudentKyc>()
            Log.d(TAG, "Student KYC fetch result: ${if (kyc != null) "Found" else "Not found"}")
            Result.success(kyc)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching student KYC for: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun getMerchantKyc(userId: String): Result<MerchantKyc?> {
        Log.d(TAG, "Fetching merchant KYC for: $userId")
        return try {
            val kyc = postgrest.from("merchant_kyc")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<MerchantKyc>()
            Log.d(TAG, "Merchant KYC fetch result: ${if (kyc != null) "Found" else "Not found"}")
            Result.success(kyc)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching merchant KYC for: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(imageBytes: ByteArray, extension: String): Result<String> {
        val userId = auth.currentUserOrNull()?.id ?: return Result.failure(Exception("Not authenticated"))
        Log.d(TAG, "Updating avatar for user: $userId")
        
        return try {
            val path = "$userId/avatar.$extension"
            val bucket = storage.from("user-profiles")
            bucket.upload(path, imageBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(path)
            Log.d(TAG, "Avatar uploaded to storage. URL: $publicUrl")

            // Update database - check which KYC exists
            val studentKyc = getStudentKyc(userId).getOrNull()
            if (studentKyc != null) {
                postgrest.from("student_kyc").update({
                    set("avatar_url", publicUrl)
                }) {
                    filter { eq("id", userId) }
                }
            } else {
                val merchantKyc = getMerchantKyc(userId).getOrNull()
                if (merchantKyc != null) {
                    postgrest.from("merchant_kyc").update({
                        set("avatar_url", publicUrl)
                    }) {
                        filter { eq("id", userId) }
                    }
                }
            }

            Log.d(TAG, "Avatar updated in database successfully.")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update avatar", e)
            Result.failure(e)
        }
    }
}
