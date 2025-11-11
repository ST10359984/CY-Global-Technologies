package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cyglobaltech.helpers.MessageBox
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {

    private lateinit var amountTextView: TextView
    private lateinit var confirmButton: Button
    private lateinit var qrCodeImage: ImageView
    private var totalAmount: Double = 0.0
    private val db = FirebaseFirestore.getInstance()
    private var currentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        amountTextView = findViewById(R.id.paymentAmountText)
        confirmButton = findViewById(R.id.confirmPaymentButton)
        qrCodeImage = findViewById(R.id.capitecQrImage)

        currentUid = UserManager.getLoggedInUid()
        if (currentUid == null) {
            Toast.makeText(this, "You must be logged in to pay", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        amountTextView.text = "Total: R${"%.2f".format(totalAmount)}"

        confirmButton.setOnClickListener {
            handlePaymentConfirmation()
        }
    }

    private fun handlePaymentConfirmation() {
        confirmButton.isEnabled = false
        Toast.makeText(this, "Confirming payment... please wait", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cartCollection = db.collection("users").document(currentUid!!).collection("cart")
                val cartSnapshot = cartCollection.get().await()

                for (document in cartSnapshot.documents) {
                    cartCollection.document(document.id).delete().await()
                }

                val order = hashMapOf(
                    "userId" to currentUid,
                    "total" to totalAmount,
                    "reference" to "Simulated_Capitec_Pay"
                )
                db.collection("orders").add(order).await()

                withContext(Dispatchers.Main) {
                    MessageBox.show(this@PaymentActivity, "Payment Confirmed",
                        "Your order has been placed successfully! We will notify you when it is ready.", false) {

                        val intent = Intent(this@PaymentActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PaymentActivity, "Confirmation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    confirmButton.isEnabled = true
                }
            }
        }
    }
}