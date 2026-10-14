package com.example.wellme.util

import com.codeskop.sdk.core.Codeskop
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.UnknownHostException

object ErrorMapper {
    fun getUserFriendlyMessage(throwable: Throwable): String {
        try {
            Codeskop.recordException(throwable)
        } catch (_: Throwable) {
            // Ignore if SDK is not initialized in unit tests
        }

        return when (throwable) {
            is UnknownHostException, is ConnectException -> 
                "No internet connection. Please check your network."
            is HttpRequestTimeoutException -> 
                "The request timed out. Please try again later."
            is ResponseException -> {
                when (throwable.response.status.value) {
                    401 -> "Invalid credentials. Please try again."
                    403 -> "You don't have permission to perform this action."
                    404 -> "The requested resource was not found."
                    409 -> "This account or record already exists."
                    429 -> "Too many requests. Please slow down."
                    in 500..599 -> "Server error. We're working on it."
                    else -> "Something went wrong on our end. (${throwable.response.status.value})"
                }
            }
            is SerializationException -> "App error: failed to process data from server."
            else -> mapRawMessageToUserFriendly(throwable.message ?: "")
        }
    }

    fun mapRawMessageToUserFriendly(rawMessage: String): String {
        val lower = rawMessage.lowercase()
        return when {
            lower.contains("amount_in_cents must be at least 100") ->
                "Amount must be at least KSh 1.00 (whole KES amount)."
            lower.contains("amount_in_cents") ->
                "Please enter a valid KSh deposit amount (minimum KSh 1.00)."
            lower.contains("phone_number") || lower.contains("invalid phone") ->
                "Please enter a valid M-Pesa phone number (e.g. 254712345678)."
            lower.contains("insufficient") || lower.contains("balance") ->
                "Insufficient balance in your wallet."
            lower.contains("failed or was cancelled") || lower.contains("cancelled") || lower.contains("canceled") ->
                "M-Pesa payment was cancelled or timed out. Please try again."
            lower.contains("timeout") || lower.contains("timed out") ->
                "Request timed out. Please check your network and try again."
            lower.contains("invalid login credentials") || lower.contains("invalid_credentials") ->
                "Incorrect email or password."
            lower.contains("user already registered") || lower.contains("already exists") ->
                "An account with this email already exists."
            lower.contains("email not confirmed") ->
                "Please verify your email address first."
            lower.contains("network") || lower.contains("connection") ->
                "Connection lost. Please try again."
            lower.contains("badrequestrestexception") || lower.contains("restexception") || lower.contains("{") ->
                extractJsonErrorOrFallback(rawMessage)
            rawMessage.isNotBlank() -> {
                if (!rawMessage.contains("io.github") && !rawMessage.contains("Exception") && !rawMessage.contains("{")) {
                    rawMessage
                } else {
                    "Unable to complete request. Please verify your details and try again."
                }
            }
            else -> "An unexpected error occurred. Please try again."
        }
    }

    private fun extractJsonErrorOrFallback(rawMessage: String): String {
        try {
            val errorRegex = """"error"\s*:\s*"([^"]+)"""".toRegex()
            val messageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()

            val match = errorRegex.find(rawMessage) ?: messageRegex.find(rawMessage)
            if (match != null) {
                val extracted = match.groupValues[1]
                return mapRawMessageToUserFriendly(extracted)
            }
        } catch (_: Exception) {}
        return "Unable to communicate with the server. Please try again later."
    }
}
