package com.example.cheap.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheap.ui.login.LoginActivity

class RegistrationActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.cheap.R.layout.activity_registration)

        editTextUsername = findViewById(com.example.cheap.R.id.editTextUsername)
        editTextEmail = findViewById(com.example.cheap.R.id.editTextEmail)
        editTextPassword = findViewById(com.example.cheap.R.id.editTextPassword)
        buttonRegister = findViewById(com.example.cheap.R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            // Perform registration logic (e.g., validate input fields, create new user account)

            // Example: Display a toast message upon successful registration
            Toast.makeText(
                this@RegistrationActivity,
                "Registration successful!",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to the login page or other appropriate screen
            startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
            finish() // Close registration activity
        }
    }
}

