package com.example.wellme.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GuestSession {
    var isGuest by mutableStateOf(false)
}
