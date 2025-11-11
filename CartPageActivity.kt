package com.example.cyglobaltech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val productId: String = ""
)

class CartPageActivity : AppCompatActivity() {

    private lateinit var cartList: RecyclerView
    private lateinit var totalText: TextView
    private lateinit var checkoutButton: Button
    private lateinit var emptyView: TextView
    private val cartItems = mutableListOf<CartItem>()
    private lateinit var adapter: CartAdapter
    private val db = FirebaseFirestore.getInstance()
    private var currentUid: String? = null
    private var currentTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_page)

        cartList = findViewById(R.id.cart_items)
        totalText = findViewById(R.id.cart_total)
        checkoutButton = findViewById(R.id.checkout_button)
        emptyView = findViewById(R.id.empty_cart_message)

        currentUid = UserManager.getLoggedInUid()

        if (currentUid == null) {
            Toast.makeText(this, "Please log in to view your cart", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        adapter = CartAdapter(cartItems) { cartItem ->
            removeFromCart(cartItem)
        }
        cartList.layoutManager = LinearLayoutManager(this)
        cartList.adapter = adapter

        checkoutButton.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty. Add items first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("TOTAL_AMOUNT", currentTotal)
            startActivity(intent)
        }

        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        displayCart()
    }

    private fun getCartCollection() = db.collection("users").document(currentUid!!).collection("cart")

    private fun displayCart() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = getCartCollection().get().await()
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                }

                withContext(Dispatchers.Main) {
                    cartItems.clear()
                    cartItems.addAll(items)
                    adapter.notifyDataSetChanged()
                    updateTotal()
                    checkEmptyState()
                    animateCartItems()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CartPageActivity, "Failed to load cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun removeFromCart(item: CartItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getCartCollection().document(item.id).delete().await()
                withContext(Dispatchers.Main) {
                    displayCart()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CartPageActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTotal() {
        currentTotal = cartItems.sumOf { it.price * it.quantity }
        totalText.text = "R${"%.2f".format(currentTotal)}"
    }

    private fun checkEmptyState() {
        emptyView.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
        cartList.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE
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

    private fun animateCartItems() {
        cartList.post {
            for (i in 0 until cartList.childCount) {
                val child = cartList.getChildAt(i)
                child.alpha = 0f
                child.scaleX = 0.9f
                child.scaleY = 0.9f
                child.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay((i * 100).toLong())
                    .setDuration(400)
                    .withEndAction {
                        child.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150)
                            .withEndAction {
                                child.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                            }.start()
                    }
                    .start()
            }
        }
    }
}


class CartAdapter(private val items: List<CartItem>, private val onRemove: (CartItem) -> Unit) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        val price: TextView = view.findViewById(R.id.item_price)
        val removeBtn: Button = view.findViewById(R.id.remove_button)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = "${item.name} (x${item.quantity})"
        holder.price.text = "R${"%.2f".format(item.price * item.quantity)}"

        holder.removeBtn.setOnClickListener {
            onRemove(item)
        }
    }

    override fun getItemCount(): Int = items.size
}