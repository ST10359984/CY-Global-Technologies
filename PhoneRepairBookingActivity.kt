package com.example.cyglobaltech

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Patterns
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class RepairBooking(
    val uid: String = "",
    val userId: String = "",
    val serviceType: String = "Phone Repair",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val device: String = "",
    val problem: String = "",
    val status: String = "Pending",
    val createdAt: FieldValue? = null
)

class PhoneRepairBookingActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var deviceInput: EditText
    private lateinit var problemInput: EditText
    private lateinit var submitButton: Button
    private lateinit var confirmationText: TextView
    private lateinit var formLayout: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private var currentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_repair)

        currentUid = UserManager.getLoggedInUid()
        if (currentUid == null) {
            Toast.makeText(this, "You must be logged in to book a repair", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameInput = findViewById(R.id.repairNameInput)
        phoneInput = findViewById(R.id.repairPhoneInput)
        emailInput = findViewById(R.id.repairEmailInput)
        deviceInput = findViewById(R.id.repairDeviceInput)
        problemInput = findViewById(R.id.repairProblemInput)
        submitButton = findViewById(R.id.repairSubmitButton)
        confirmationText = findViewById(R.id.repairConfirmationText)
        formLayout = findViewById(R.id.formLayout)

        submitButton.setOnClickListener { submitRepairRequest() }
    }

    private fun submitRepairRequest() {
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val device = deviceInput.text.toString().trim()
        val problem = problemInput.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || device.isEmpty() || problem.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        submitButton.isEnabled = false

        val newRepairBooking = RepairBooking(
            userId = currentUid!!,
            name = name,
            phone = phone,
            email = email,
            device = device,
            problem = problem,
            status = "Pending",
            createdAt = FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("repairBookings").add(newRepairBooking).await()
                withContext(Dispatchers.Main) {
                    val message = """
                        Booking Confirmed!
                        Name: $name
                        Phone: $phone
                        Email: $email
                        Device: $device
                        Problem: $problem
                    """.trimIndent()

                    confirmationText.text = message
                    confirmationText.visibility = View.VISIBLE
                    formLayout.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PhoneRepairBookingActivity, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    submitButton.isEnabled = true
                }
            }
        }
    }
}