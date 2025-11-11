package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.helpers.MessageBox
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileFullName: TextView
    private lateinit var profileEmailDisplay: TextView
    private lateinit var profilePhoneDisplay: TextView
    private lateinit var editProfileBtn: Button
    private lateinit var saveProfileBtn: Button
    private lateinit var cancelEditBtn: Button
    private lateinit var logoutButton: Button
    private lateinit var adminDashboardButton: Button // ADDED
    private lateinit var profileInfoDisplay: LinearLayout
    private lateinit var profileInfoEdit: LinearLayout
    private lateinit var editName: EditText
    private lateinit var editSurname: EditText
    private lateinit var editPhone: EditText
    private lateinit var loginPrompt: TextView

    private var loggedInUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileFullName = findViewById(R.id.profile_full_name)
        profileEmailDisplay = findViewById(R.id.profile_email_display)
        profilePhoneDisplay = findViewById(R.id.profile_phone_display)
        editProfileBtn = findViewById(R.id.edit_profile_btn)
        saveProfileBtn = findViewById(R.id.save_profile_btn)
        cancelEditBtn = findViewById(R.id.cancel_edit_btn)
        logoutButton = findViewById(R.id.logout_button)
        adminDashboardButton = findViewById(R.id.adminDashboardButton) // ADDED
        profileInfoDisplay = findViewById(R.id.profile_info_display)
        profileInfoEdit = findViewById(R.id.profile_info_edit)
        editName = findViewById(R.id.edit_name)
        editSurname = findViewById(R.id.edit_surname)
        editPhone = findViewById(R.id.edit_phone)
        loginPrompt = findViewById(R.id.login_prompt)

        loggedInUid = UserManager.getLoggedInUid()
        if (loggedInUid == null) {
            showLoginPrompt()
            return
        }

        loadUserInfo()
        checkAdminStatus()

        editProfileBtn.setOnClickListener { showEditFields() }
        cancelEditBtn.setOnClickListener { hideEditFields() }
        saveProfileBtn.setOnClickListener { saveProfileChanges() }
        logoutButton.setOnClickListener { logout() }

        adminDashboardButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun showLoginPrompt() {
        loginPrompt.visibility = View.VISIBLE
        profileInfoDisplay.visibility = View.GONE
        editProfileBtn.visibility = View.GONE
        logoutButton.visibility = View.GONE
        adminDashboardButton.visibility = View.GONE
    }

    private fun loadUserInfo() {
        loggedInUid?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                val userProfile = UserManager.getUserProfile(uid)
                withContext(Dispatchers.Main) {
                    if (userProfile != null) {
                        profileFullName.text = "${userProfile.name} ${userProfile.surname}"
                        profileEmailDisplay.text = userProfile.email
                        profilePhoneDisplay.text = userProfile.phone.ifEmpty { "Not set" }

                        editName.setText(userProfile.name)
                        editSurname.setText(userProfile.surname)
                        editPhone.setText(userProfile.phone)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkAdminStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val isAdmin = withContext(Dispatchers.IO) {
                UserManager.isAdmin()
            }
            if (isAdmin) {
                adminDashboardButton.visibility = View.VISIBLE
            }
        }
    }

    private fun showEditFields() {
        profileInfoDisplay.visibility = View.GONE
        editProfileBtn.visibility = View.GONE
        profileInfoEdit.visibility = View.VISIBLE
    }

    private fun hideEditFields() {
        profileInfoEdit.visibility = View.GONE
        profileInfoDisplay.visibility = View.VISIBLE
        editProfileBtn.visibility = View.VISIBLE
    }

    private fun saveProfileChanges() {
        val newName = editName.text.toString().trim()
        val newSurname = editSurname.text.toString().trim()
        val newPhone = editPhone.text.toString().trim()

        if (newName.isEmpty() || newSurname.isEmpty()) {
            Toast.makeText(this, "Name and Surname cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        saveProfileBtn.isEnabled = false
        loggedInUid?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                val (success, message) = UserManager.updateProfile(uid, newName, newSurname, newPhone)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                        hideEditFields()
                        loadUserInfo()
                    } else {
                        MessageBox.show(this@ProfileActivity, "Error", message ?: "Failed to update profile.")
                    }
                    saveProfileBtn.isEnabled = true
                }
            }
        }
    }

    private fun logout() {
        UserManager.logout(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_services -> {
                    startActivity(Intent(this, ServicesActivity::class.java))
                    true
                }
                R.id.nav_products -> {
                    startActivity(Intent(this, ProductsActivity::class.java))
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}