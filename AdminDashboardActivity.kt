package com.example.cyglobaltech

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var productNameInput: EditText
    private lateinit var productTypeInput: EditText
    private lateinit var productPriceInput: EditText
    private lateinit var addProductButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var viewAsUserButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this).load(uri).into(imagePreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        productNameInput = findViewById(R.id.adminProductName)
        productTypeInput = findViewById(R.id.adminProductType)
        productPriceInput = findViewById(R.id.adminProductPrice)
        addProductButton = findViewById(R.id.adminAddProductButton)
        selectImageButton = findViewById(R.id.adminSelectImageButton)
        imagePreview = findViewById(R.id.adminProductImageView)
        viewAsUserButton = findViewById(R.id.adminViewAsUserButton)

        addProductButton.setOnClickListener {
            addNewProduct()
        }

        selectImageButton.setOnClickListener {
            openGallery()
        }

        viewAsUserButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun addNewProduct() {
        val name = productNameInput.text.toString().trim()
        val type = productTypeInput.text.toString().trim()
        val priceStr = productPriceInput.text.toString().trim()

        if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all product fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageAndAddProduct(name, type, price, selectedImageUri!!)
    }

    private fun uploadImageAndAddProduct(name: String, type: String, price: Double, imageUri: Uri) {
        addProductButton.isEnabled = false
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        val fileName = "${System.currentTimeMillis()}"
        val storageRef = storage.reference.child("product_images/$fileName")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (imageUri == null) {
                    throw IllegalArgumentException("Image URI is null.")
                }

                val uploadTask = storageRef.putFile(imageUri).await()

                val downloadUrl = storageRef.downloadUrl.await().toString()

                val newProduct = hashMapOf(
                    "name" to name,
                    "category" to type,
                    "price" to price,
                    "imageRes" to downloadUrl
                )

                db.collection("products").add(newProduct).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    productNameInput.text.clear()
                    productTypeInput.text.clear()
                    productPriceInput.text.clear()
                    imagePreview.setImageURI(null)
                    selectedImageUri = null
                    addProductButton.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Log the error for better debugging
                    e.printStackTrace()
                    Toast.makeText(this@AdminDashboardActivity, "Failed to add product: ${e.message}", Toast.LENGTH_LONG).show()
                    addProductButton.isEnabled = true
                }
            }
        }
    }
}