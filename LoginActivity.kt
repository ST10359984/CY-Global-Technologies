package com.example.cyglobaltech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.helpers.MessageBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerRedirectButton: Button
    private lateinit var forgotPasswordRedirectButton: TextView
    private lateinit var rememberMeCheckbox: CheckBox

    private val prefsName = "login_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindViews()
        if (checkAutoLogin()) return

        loadSavedCredentials()
        setupClickListeners()
    }

    private fun bindViews() {
        emailInput = findViewById(R.id.et_login_email_username)
        passwordInput = findViewById(R.id.et_login_password)
        loginButton = findViewById(R.id.btn_login)
        registerRedirectButton = findViewById(R.id.btn_register)
        forgotPasswordRedirectButton = findViewById(R.id.tv_forgot_password)
        rememberMeCheckbox = findViewById(R.id.cb_remember_me)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener { attemptLogin() }
        registerRedirectButton.setOnClickListener { navigateTo(RegisterActivity::class.java) }
        forgotPasswordRedirectButton.setOnClickListener { navigateTo(ForgotPasswordActivity::class.java) }

        findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            Toast.makeText(this, "Google Login Integration Pending", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields.", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val (success, message) = UserManager.validateLogin(this@LoginActivity, email, password)

            withContext(Dispatchers.Main) {
                if (success) {
                    saveCredentials(email, rememberMeCheckbox.isChecked)
                    MessageBox.show(this@LoginActivity, "Success", "Login successful!", false) {
                        navigateTo(MainActivity::class.java, finishCurrent = true)
                    }
                } else {
                    val shake = AnimationUtils.loadAnimation(this@LoginActivity, R.anim.shake)
                    loginButton.startAnimation(shake)
                    Toast.makeText(this@LoginActivity, message ?: "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    loginButton.isEnabled = true
                }
            }
        }
    }

    private fun saveCredentials(email: String, remember: Boolean) {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (remember) {
                putString("saved_email", email)
            } else {
                remove("saved_email")
            }
            putBoolean("remember_me", remember)
            apply()
        }
    }

    private fun loadSavedCredentials() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val remember = prefs.getBoolean("remember_me", false)
        if (remember) {
            emailInput.setText(prefs.getString("saved_email", ""))
            rememberMeCheckbox.isChecked = true
        }
    }

    private fun checkAutoLogin(): Boolean {
        if (UserManager.getLoggedInUid() != null) {
            navigateTo(MainActivity::class.java, finishCurrent = true)
            return true
        }
        return false
    }

    private fun navigateTo(target: Class<*>, finishCurrent: Boolean = false) {
        startActivity(Intent(this, target))
        if (finishCurrent) finish()
    }
}