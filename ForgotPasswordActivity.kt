package com.example.cyglobaltech

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.helpers.MessageBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var changePasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailInput = findViewById(R.id.forgot_email_input)
        changePasswordButton = findViewById(R.id.change_password_button)

        findViewById<EditText>(R.id.forgot_new_password_input).visibility = View.GONE
        findViewById<EditText>(R.id.forgot_password_confirm_new_password).visibility = View.GONE

        changePasswordButton.setOnClickListener {
            attemptPasswordReset()
        }
    }

    private fun attemptPasswordReset() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            MessageBox.show(this, "Missing Info", "Please enter your email address.")
            return
        }

        changePasswordButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val (success, message) = UserManager.changePassword(email)

            withContext(Dispatchers.Main) {
                if (success) {
                    MessageBox.show(this@ForgotPasswordActivity, "Email Sent",
                        message ?: "A password reset link has been sent to your email.",
                        false) {
                        finish()
                    }
                } else {
                    MessageBox.show(this@ForgotPasswordActivity, "Error",
                        message ?: "Failed to send reset email. Ensure the email is correct.")
                    changePasswordButton.isEnabled = true
                }
            }
        }
    }
}