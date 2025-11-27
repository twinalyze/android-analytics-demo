package com.twinalyze.servicedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.Order

class OrdersDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val txtTitleOrderId = findViewById<TextView>(R.id.txtTitleOrderId)
        val txtOrderId = findViewById<TextView>(R.id.txtOrderId)
        val txtDateTime = findViewById<TextView>(R.id.txtDateTime)
        val txtPending = findViewById<TextView>(R.id.txtPending)
        val txtRestaurantName = findViewById<TextView>(R.id.txtRestaurantName)
        val txtAddress = findViewById<TextView>(R.id.txtAddress)
        val layoutFoodItems = findViewById<LinearLayout>(R.id.layoutFoodItems)
        val txtSubTotal = findViewById<TextView>(R.id.txtSubTotal)
        val txtDiscount = findViewById<TextView>(R.id.txtDiscount)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val btnCancelOrder = findViewById<CardView>(R.id.btnCancelOrder)
        val btnComplete = findViewById<CardView>(R.id.btnComplete)


        val orderId = intent.getStringExtra("OrderId")
        val orderDate = intent.getStringExtra("OrderDate")
        val restaurantName = intent.getStringExtra("RestaurantName")
        val restaurantAddress = intent.getStringExtra("RestaurantAddress")
//        val json = intent.getStringExtra("OrderItemListJson")
//        val type = object : TypeToken<List<Food>>() {}.type
//        val orderItems: List<Food> = Gson().fromJson(json, type)

        val orderItems = intent.getSerializableExtra("OrderItemList") as? ArrayList<Food>
        Log.d("ordersAdapter", "onCreate ordersAdapter: "+orderItems)


        txtTitleOrderId.text = orderId
        txtOrderId.text = orderId
        txtDateTime.text = orderDate
        txtRestaurantName.text = restaurantName
        txtAddress.text = restaurantAddress

        var subTotal = 0

        for (food in orderItems ?: emptyList()) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_food_row, layoutFoodItems, false)
            val txtFoodName = row.findViewById<TextView>(R.id.txtFoodName)
            val txtPrice = row.findViewById<TextView>(R.id.txtPrice)

            txtFoodName.text = "1x ${food.name}"
            txtPrice.text = "${food.price}"

            layoutFoodItems.addView(row)

            // 🔹 Add price * quantity to subtotal
            val unitPrice = food.price.toIntOrNull() ?: 0
            subTotal += (unitPrice * food.quantity)
        }


        // 🔹 After loop — calculate subtotal, discount, total
        txtSubTotal.text = subTotal.toString()

        val sub = txtSubTotal.text.toString().toIntOrNull() ?: 0
        val discount = txtDiscount.text.toString().toIntOrNull() ?: 0
        val total = (sub - discount).coerceAtLeast(0)

        txtTotal.text = total.toString()

        // 🔹 Dynamically add food items to layout
//        for (food in orderItems) {
//            val row = LayoutInflater.from(this).inflate(R.layout.item_food_row, layoutFoodItems, false)
//
//            val txtFoodName = row.findViewById<TextView>(R.id.txtFoodName)
//            val txtPrice = row.findViewById<TextView>(R.id.txtPrice)
//
//            txtFoodName.text = "1x ${food.name}"
//            txtPrice.text = "${food.price}"
//
//            layoutFoodItems.addView(row)
//        }


        btnBack.setOnClickListener {
            onBackPressed()
        }

//        btnCancelOrder.setOnClickListener {
//
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("OrdersDetailsActivity", "OrdersDetailsActivity")
//            intent.putExtra("restaurantName", restaurantName)
//            intent.putExtra("restaurantAddress", restaurantAddress)
//            val json = Gson().toJson(orderItems)
//            intent.putExtra("food_list", json)
//            startActivity(intent)
//
//        }

        btnComplete.setOnClickListener {
            updateOrderStatus(orderId, "Complete")
        }

        btnCancelOrder.setOnClickListener {
            updateOrderStatus(orderId, "Cancelled")
        }

//        intent.putExtra("orderId", "#${order.orderId}")
//        intent.putExtra("restaurantName", order.restaurantName)
    }

    fun saveOrdersToPrefs(context: Context, orders: List<Order>) {
        val gson = Gson()
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("orders_list", gson.toJson(orders)).apply()
    }

    private fun updateOrderStatus(orderId: String?, newStatus: String) {
        if (orderId.isNullOrEmpty()) return

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val savedJson = prefs.getString("orders_list", null)

        val type = object : TypeToken<MutableList<Order>>() {}.type
        val savedOrders: MutableList<Order> = if (!savedJson.isNullOrEmpty()) {
            gson.fromJson(savedJson, type)
        } else {
            mutableListOf()
        }

        // 🔹 Find the order with matching ID and update status
        val targetOrder = savedOrders.firstOrNull { it.orderId == orderId }
        if (targetOrder != null) {
            targetOrder.status = newStatus
            prefs.edit().putString("orders_list", gson.toJson(savedOrders)).apply()
            Log.d("OrderUpdate", "✅ Order $orderId marked as $newStatus")

            Toast.makeText(this, "Order marked as $newStatus", Toast.LENGTH_SHORT).show()

            // Optional: Go back to Orders screen
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragmentName", "CartFragment")
            startActivity(intent)
            finish()
        } else {
            Log.d("OrderUpdate", "⚠️ No order found with id $orderId")
        }
    }

}