package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (UserManager.getLoggedInUid() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        findViewById<ImageView>(R.id.cartButton).setOnClickListener {
            startActivity(Intent(this, CartPageActivity::class.java))
        }
    }

    fun goToInternetCafe(view: View) {
        startActivity(Intent(this, InternetCafeBookingActivity::class.java))
    }

    fun goToPhoneRepair(view: View) {
        startActivity(Intent(this, PhoneRepairBookingActivity::class.java))
    }

    fun goToPrintDocuments(view: View) {
        startActivity(Intent(this, PrintUploadActivity::class.java))
    }

    fun goToProducts(view: View) {
        startActivity(Intent(this, ProductsActivity::class.java))
    }

    fun addMonitorToCart(view: View) {
        val monitor = Product(
            id = "placeholder_monitor_id",
            name = "SpectraTech 24-inch FHD Monitor",
            price = 150.00
        )
        CoroutineScope(Dispatchers.Main).launch {
            val (success, message) = CartManager.addToCart(monitor)
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun addKeyboardToCart(view: View) {
        val keyboard = Product(
            id = "placeholder_keyboard_id",
            name = "Zenith Innovations Mechanical Gaming Keyboard",
            price = 300.00
        )
        CoroutineScope(Dispatchers.Main).launch {
            val (success, message) = CartManager.addToCart(keyboard)
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun logout() {
        UserManager.logout(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}