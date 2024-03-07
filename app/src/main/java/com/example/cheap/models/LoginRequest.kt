package com.example.cheap.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val id: String?,
    val username: String?,
    val success: Boolean,
    val accessToken: String?,
    val refreshToken: String?,
    val message: String?
)

