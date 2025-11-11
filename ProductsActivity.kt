package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageRes: String = ""
)

class ProductsActivity : AppCompatActivity() {

    private lateinit var productsContainer: LinearLayout
    private lateinit var spinnerFilter: Spinner
    private lateinit var searchEditText: EditText
    private lateinit var allProducts: List<Product>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        productsContainer = findViewById(R.id.productsContainer)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        searchEditText = findViewById(R.id.searchProductsEditText)
        allProducts = emptyList()

        findViewById<ImageView>(R.id.cartButton).setOnClickListener {
            startActivity(Intent(this, CartPageActivity::class.java))
        }

        setupFilters()
        loadProducts()
        setupBottomNavigation()
    }

    private fun setupFilters() {
        val categories = listOf("All", "Monitors", "Keyboards", "Mice", "Headsets", "Webcams", "Printers")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                filterAndDisplayProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndDisplayProducts() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("products").get().await()
                allProducts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }

                withContext(Dispatchers.Main) {
                    filterAndDisplayProducts()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductsActivity, "Failed to load products: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterAndDisplayProducts() {
        val selectedCategory = spinnerFilter.selectedItem.toString()
        val searchQuery = searchEditText.text.toString().lowercase()
        val filteredProducts = allProducts.filter {
            (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                    it.name.lowercase().contains(searchQuery)
        }
        displayProducts(filteredProducts)
    }

    private fun displayProducts(products: List<Product>) {
        productsContainer.removeAllViews()
        val inflater = layoutInflater
        if (products.isEmpty()) {
            val noResultsView = TextView(this)
            noResultsView.text = "No products found."
            noResultsView.gravity = android.view.Gravity.CENTER
            noResultsView.setPadding(16, 16, 16, 16)
            productsContainer.addView(noResultsView)
            return
        }

        for (product in products) {
            val itemView = inflater.inflate(R.layout.product_item, productsContainer, false)
            itemView.findViewById<TextView>(R.id.productName).text = product.name
            itemView.findViewById<TextView>(R.id.productPrice).text = "R${String.format("%.2f", product.price)}"

            val imageView = itemView.findViewById<ImageView>(R.id.productImage)

            if (product.imageRes.isNotEmpty()) {
                Glide.with(this)
                    .load(product.imageRes)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder)
            }

            itemView.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val (success, message) = CartManager.addToCart(product)
                    Toast.makeText(this@ProductsActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            productsContainer.addView(itemView)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_products

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
                R.id.nav_products -> true
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