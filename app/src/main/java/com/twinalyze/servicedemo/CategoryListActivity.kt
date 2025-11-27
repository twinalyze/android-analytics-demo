package com.twinalyze.servicedemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.alldatget.AllScreenTracker
import com.twinalyze.event.SetAnalytics
import com.twinalyze.servicedemo.adapter.FoodAdapter
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.FoodItem
import com.twinalyze.servicedemo.model.RestaurantModel

class CategoryListActivity : AppCompatActivity() {

    private lateinit var recyclerCategories: RecyclerView
    private val prefs by lazy {
        getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }
    private var cartCount = 0  // 🔢 badge counter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        putExtra("category_name", clickedItem.foodCategories)
//        putExtra("category_items_json", json)

        SetAnalytics.getInstance()
            .setActivityEvent(
                "CategoryListActivity manual",    // screenName
                this@CategoryListActivity // screenClass
            )

        val itemsJson   = intent.getStringExtra("category_items_json").orEmpty()
        val categoryName = intent.getStringExtra("category_name").orEmpty()

        Log.d("CategoryList", "name=$categoryName jsonLength=${itemsJson}")


        recyclerCategories = findViewById(R.id.recyclerCategories)
        if (itemsJson.isBlank()) {
            Toast.makeText(this, "No items found for this category.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Parse: JSON -> ArrayList<FoodItem>
        val listType = object : TypeToken<ArrayList<FoodItem>>() {}.type
        val items: ArrayList<FoodItem> = Gson().fromJson<ArrayList<FoodItem>>(itemsJson, listType) ?: arrayListOf()

        // (Optional) show category title if you have a TextView with this id

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = intent.getStringExtra("category_name") ?: "Categories"
//        findViewById<Toolbar?>(R.id.toolbar)?.text = categoryName
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }


        // Setup RecyclerView
        recyclerCategories.layoutManager = LinearLayoutManager(this)
        recyclerCategories.adapter = FoodAdapter(items) { item ->
            // Add single item to cart (adjust if your FoodAdapter sends a different type)
            addToCart(item)
        }


    }

    private fun addToCart(item: FoodItem) {
        val gson = Gson()
        val cartType = object : TypeToken<MutableList<Food>>() {}.type

        val existingCartJson = prefs.getString("cart_json", null)
        val currentCart: MutableList<Food> =
            if (existingCartJson.isNullOrEmpty()) mutableListOf()
            else gson.fromJson(existingCartJson, cartType)

        currentCart += Food(
            item.foodName.orEmpty(),
            item.foodPrice.orEmpty(),
            item.foodImage.orEmpty(),
            1
        )

        prefs.edit().putString("cart_json", gson.toJson(currentCart)).apply()
        prefs.edit().putInt("cart_count", currentCart.size).apply()

        Toast.makeText(this, "${item.foodName} added to cart", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (AllScreenTracker.getInstance().isManualAppForeground) {
            SetAnalytics.getInstance()
                .setActivityEvent(
                    "CategoryListActivity Foreground Manual", // screenName
                    this@CategoryListActivity            // screenClass
                )
        }
    }
}