package com.example.wellme.util

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.UnknownHostException

object ErrorMapper {
    fun getUserFriendlyMessage(throwable: Throwable): String {
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
            else -> {
                val message = throwable.message ?: ""
                when {
                    throwable is com.example.wellme.domain.usecase.IllegalEcosystemException -> message
                    throwable is com.example.wellme.domain.usecase.InsufficientBalanceException -> message
                    message.contains("Invalid login credentials", ignoreCase = true) -> 
                        "Incorrect email or password."
                    message.contains("User already registered", ignoreCase = true) -> 
                        "An account with this email already exists."
                    message.contains("Email not confirmed", ignoreCase = true) -> 
                        "Please verify your email address first."
                    message.contains("network", ignoreCase = true) -> 
                        "Connection lost. Please try again."
                    else -> "An unexpected error occurred. Please try again."
                }
            }
        }
    }
}
