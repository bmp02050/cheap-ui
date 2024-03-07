package com.example.cheap.ui.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cheap.MainActivity
import com.example.cheap.R
import com.example.cheap.models.LoginRequest
import com.example.cheap.models.RefreshTokenRequest
import com.example.cheap.services.RetrofitClient
import com.example.cheap.ui.registration.RegistrationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegistration: Button
    private val REQUEST_CODE_INTERNET_PERMISSION = 100 // Define the request code constant
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasInternetPermission()) {
            requestInternetPermission()
        }
        if (!checkLocationPermission()) {
            requestLocationPermission()
        }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", null)
        val refreshToken = sharedPreferences.getString("refreshToken", null)
        val userId = sharedPreferences.getString("userId", null)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val savedTimeString = sharedPreferences.getString("validUntil", null)
        val clientService = RetrofitClient.getClientService(accessToken)
        val currentTime = Date()
        if (!refreshToken.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val refreshTokenRequest = RefreshTokenRequest(userId, refreshToken)
                    val refreshTokenResponse = clientService.refreshToken(refreshTokenRequest);
                    if (refreshTokenResponse.isSuccessful) {
                        saveTokens(
                            refreshTokenResponse.body()?.accessToken ?: "",
                            refreshTokenResponse.body()?.refreshToken ?: "",
                            refreshTokenResponse.body()?.id ?: ""
                        )
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish() // Finish login activity
                    }
                } catch (e: Exception) {
                    // Handle network or unexpected errors
                    showToast("Error: ${e.message}")
                }
            }
        }else if (!accessToken.isNullOrEmpty()) {
            // Token available, skip login page
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish() // Finish login activity
        }
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsernameOrEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegistration = findViewById(R.id.buttonRegistration)

        buttonLogin.setOnClickListener {
            val usernameOrEmail = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            val loginRequest = LoginRequest(usernameOrEmail, password)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = clientService.login(loginRequest)
                    if (response.isSuccessful) {
                        val loginResponse = response.body()

                        // Login successful
                        showToast(loginResponse?.message ?: "Login successful")
                        saveTokens(
                            loginResponse?.accessToken ?: "",
                            loginResponse?.refreshToken ?: "",
                            loginResponse?.id ?: ""
                        )
                        // Navigate to the main activity or other appropriate screen
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    } else {
                        // Handle API error
                        showToast("Error: ${response.message()}")
                    }
                } catch (e: Exception) {
                    // Handle network or unexpected errors
                    showToast("Error: ${e.message}")
                }
            }
        }

        buttonRegistration.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasInternetPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.INTERNET),
            REQUEST_CODE_INTERNET_PERMISSION
        )
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        // Save token to SharedPreferences
        sharedPreferences.edit().putString("accessToken", accessToken).apply()
        sharedPreferences.edit().putString("refreshToken", refreshToken).apply()
        sharedPreferences.edit().putString("userId", userId).apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_INTERNET_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with internet-related tasks
            } else {
                // Permission denied, handle accordingly (e.g., show an explanation, disable features)
            }
        }
    }
}
