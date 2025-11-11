package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Booking(
    val uid: String = "",
    val userId: String = "",
    val serviceType: String = "Internet Cafe",
    val date: String = "",
    val time: String = "",
    val duration: String = "",
    val numUsers: String = "",
    val status: String = "Pending",
    val createdAt: FieldValue? = null
)

class InternetCafeBookingActivity : AppCompatActivity() {

    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var durationInput: EditText
    private lateinit var numUsersInput: EditText
    private lateinit var confirmationSection: LinearLayout
    private lateinit var formLayout: LinearLayout
    private lateinit var bookingDateView: TextView
    private lateinit var bookingTimeView: TextView
    private lateinit var bookingDurationView: TextView
    private lateinit var bookingUsersView: TextView
    private lateinit var submitButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var currentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_cafe_booking)

        currentUid = UserManager.getLoggedInUid()
        if (currentUid == null) {
            Toast.makeText(this, "You must be logged in to book", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dateInput = findViewById(R.id.date)
        timeInput = findViewById(R.id.time)
        durationInput = findViewById(R.id.duration)
        numUsersInput = findViewById(R.id.num_users)
        confirmationSection = findViewById(R.id.internet_cafe_booking_confirmation)
        formLayout = findViewById(R.id.formLayout)
        bookingDateView = findViewById(R.id.cafe_booking_date)
        bookingTimeView = findViewById(R.id.cafe_booking_time)
        bookingDurationView = findViewById(R.id.cafe_booking_duration)
        bookingUsersView = findViewById(R.id.cafe_booking_users)
        submitButton = findViewById(R.id.submit_button)

        submitButton.setOnClickListener { submitBooking() }

        setupBottomNav()
    }

    private fun submitBooking() {
        val date = dateInput.text.toString().trim()
        val time = timeInput.text.toString().trim()
        val duration = durationInput.text.toString().trim()
        val users = numUsersInput.text.toString().trim()

        if (date.isEmpty() || time.isEmpty() || duration.isEmpty() || users.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        submitButton.isEnabled = false

        val newBooking = Booking(
            userId = currentUid!!,
            date = date,
            time = time,
            duration = duration,
            numUsers = users,
            status = "Pending",
            createdAt = FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("bookings").add(newBooking).await()
                withContext(Dispatchers.Main) {
                    bookingDateView.text = date
                    bookingTimeView.text = time
                    bookingDurationView.text = duration
                    bookingUsersView.text = users

                    formLayout.visibility = View.GONE
                    confirmationSection.visibility = View.VISIBLE
                    submitButton.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InternetCafeBookingActivity, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    submitButton.isEnabled = true
                }
            }
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}