package com.example.cheap.models

data class RefreshTokenRequest(
    val userId: String?,
    val refreshToken: String
)
