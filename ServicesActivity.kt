package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val btnBookInternet = findViewById<Button>(R.id.btn_book_internet_cafe)
        val btnBookRepair = findViewById<Button>(R.id.btn_book_repair)
        val btnTrackRepair = findViewById<Button>(R.id.btn_track_repair)
        val etRepairId = findViewById<EditText>(R.id.et_repair_track_id)
        val btnPrintUpload = findViewById<Button>(R.id.btn_print_upload)

        btnBookInternet.setOnClickListener {
            startActivity(Intent(this, InternetCafeBookingActivity::class.java))
        }

        btnBookRepair.setOnClickListener {
            startActivity(Intent(this, PhoneRepairBookingActivity::class.java))
        }

        btnTrackRepair.setOnClickListener {
            val refId = etRepairId.text.toString().trim()
            if (refId.isEmpty()) {
                Toast.makeText(this, "Please enter a repair reference number", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Tracking repair ID: $refId (Pending)", Toast.LENGTH_SHORT).show()
            }
        }

        btnPrintUpload.setOnClickListener {
            startActivity(Intent(this, PrintUploadActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_services

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_services -> true

                R.id.nav_products -> {
                    startActivity(Intent(this, ProductsActivity::class.java))
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}