package com.example.wellme.util

import javax.inject.Inject

interface Base64Encoder {
    fun encode(data: ByteArray): String
}

class AndroidBase64Encoder @Inject constructor() : Base64Encoder {
    override fun encode(data: ByteArray): String {
        return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
    }
}
