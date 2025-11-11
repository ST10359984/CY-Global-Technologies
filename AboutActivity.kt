package com.example.cyglobaltech

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.databinding.ActivityAboutBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewsToAnimate = listOf(
            binding.logoImage,
            binding.cartButton,
            binding.titleText,
        binding.ourStoryTitle,
        binding.ourStoryContent,
        binding.contactInfoTitle,
        binding.locationText,
        binding.phoneText,
        binding.emailText,
        binding.btnViewMap,
        binding.footerInfo
        )

        var delay = 0L
        viewsToAnimate.forEach { view ->
            view.alpha = 0f
            view.scaleX = 0.9f
            view.scaleY = 0.9f
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .setDuration(400)
                .start()
            delay += 150
        }

        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartPageActivity::class.java))
        }

        binding.btnViewMap.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:-26.1935,28.0323?q=30 De Beer St, Braamfontein, Johannesburg")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        binding.phoneText.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0113396789"))
            startActivity(intent)
        }

        binding.emailText.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cytech69@gmail.com"))
            startActivity(intent)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_about

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
                R.id.nav_about -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}