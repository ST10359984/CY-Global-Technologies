package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.helpers.MessageBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameEditText = findViewById(R.id.nameInput)
        surnameEditText = findViewById(R.id.surnameInput)
        emailEditText = findViewById(R.id.emailInput)
        passwordEditText = findViewById(R.id.passwordInput)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)

        registerButton.setOnClickListener { attemptRegistration() }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptRegistration() {
        val name = nameEditText.text.toString().trim()
        val surname = surnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            MessageBox.show(this, "Missing Info", "Please fill in all fields.")
            return
        }
        if (password != confirmPassword) {
            MessageBox.show(this, "Password Error", "Passwords do not match.")
            return
        }
        if (password.length < 6) {
            MessageBox.show(this, "Weak Password", "Password must be at least 6 characters.")
            return
        }

        registerButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val (success, message) = UserManager.registerUser(this@RegisterActivity, name, surname, email, password)

            withContext(Dispatchers.Main) {
                if (success) {
                    MessageBox.show(this@RegisterActivity, "Success",
                        "Registration successful! A verification email has been sent. Please login.", false) {
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    MessageBox.show(this@RegisterActivity, "Error",
                        message ?: "Registration failed. Please try again.")
                    registerButton.isEnabled = true
                }
            }
        }
    }
}