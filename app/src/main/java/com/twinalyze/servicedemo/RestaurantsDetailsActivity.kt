package com.twinalyze.servicedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.alldatget.AllScreenTracker
import com.twinalyze.event.SetAnalytics
import com.twinalyze.servicedemo.adapter.FoodAdapter
import com.twinalyze.servicedemo.adapter.FoodCategoryAdapter
import com.twinalyze.servicedemo.fragment.CartFragment
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantsDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerFood: RecyclerView
    private lateinit var badge: TextView
    private lateinit var btnBack: ImageView
    private lateinit var imgRestaurantDetail: ImageView
    private lateinit var txtRestaurantName: TextView
    private lateinit var txtRating: TextView
    private lateinit var txtType: TextView
    private lateinit var txtDistance: TextView
    private lateinit var txtDescription: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var cartDetail: View

    private var cartCount = 0  // 🔢 badge counter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurants_details)

        SetAnalytics.getInstance()
            .setActivityEvent(
                "RestaurantsDetailsActivity Manual",    // screenName
                this@RestaurantsDetailsActivity // screenClass
            )

        recyclerCategories = findViewById<RecyclerView>(R.id.recyclerFoodCategories)
        recyclerFood = findViewById<RecyclerView>(R.id.recyclerFood)
        badge = findViewById<TextView>(R.id.txtCartBadge)
        btnBack = findViewById<ImageView>(R.id.btn_back)
        imgRestaurantDetail = findViewById<ImageView>(R.id.imgRestaurantDetail)
        txtRestaurantName = findViewById<TextView>(R.id.txtRestaurantName)
        txtRating = findViewById<TextView>(R.id.txtRating)
        txtType = findViewById<TextView>(R.id.txtType)
        txtDistance = findViewById<TextView>(R.id.txtDistance)
        txtDescription = findViewById<TextView>(R.id.txtDescription)
        drawerLayout = findViewById(R.id.drawerLayout)
        cartDetail = findViewById(R.id.cart_detail)


        setupDrawer()

        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        cartCount = prefs.getInt("cart_count", 0)
        updateCartCount(cartCount)

        // Get data from intent
        val name = intent.getStringExtra("restaurantName")
        val star = intent.getStringExtra("restaurantStar")
        val address = intent.getStringExtra("restaurantAddress")
        val type = intent.getStringExtra("restaurantType")
        val near = intent.getStringExtra("restaurantNear")
        val imageUrl = intent.getStringExtra("restaurantImage")
        val description = intent.getStringExtra("restaurantDescription")

        txtRestaurantName.text = name
        txtRating.text = star
        txtType.text = type
        txtDistance.text = near
        txtDescription.text = description

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_restaurant)
            .into(imgRestaurantDetail)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerFood.layoutManager = LinearLayoutManager(this)
        recyclerCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 🔹 Use cached restaurant data (or load if missing)
        lifecycleScope.launch {
            try {
                // Ensure data is loaded (if not loaded by Splash already)
                RestaurantRepository.preload(applicationContext)

                val allRestaurants = RestaurantRepository.restaurantList
                val allFoods = RestaurantRepository.foodList

                // Find this restaurant’s foods (fallback: all foods)
                val currentRestaurantFoods: List<FoodItem> = withContext(Dispatchers.Default) {
                    val matched = if (!name.isNullOrBlank()) {
                        allRestaurants
                            .firstOrNull { it.restaurantName == name }
                            ?.food
                            ?.filterNotNull()
                    } else null

                    matched ?: allFoods
                }

                // Collect unique tags for this restaurant’s menu
                val allTags: List<String?> = withContext(Dispatchers.Default) {
                    currentRestaurantFoods
                        .flatMap { it.tags ?: emptyList() }
                        .distinct()
                }

                withContext(Dispatchers.Main) {
                    // Food adapter with add-to-cart
                    val foodAdapter = FoodAdapter(currentRestaurantFoods.toMutableList()) { foodItem ->
                        cartCount += 1

                        val gson = Gson()
                        val type = object : TypeToken<MutableList<Food>>() {}.type

                        val cartJson = prefs.getString("cart_json", null)
                        val cart: MutableList<Food> =
                            if (cartJson.isNullOrEmpty()) mutableListOf()
                            else gson.fromJson(cartJson, type)

                        cart += Food(
                            foodItem.foodName.orEmpty(),
                            foodItem.foodPrice.orEmpty(),
                            foodItem.foodImage.orEmpty(),
                            1
                        )

                        prefs.edit()
                            .putString("cart_json", gson.toJson(cart))
                            .putInt("cart_count", cartCount)
                            .apply()

                        updateCartCount(cartCount)
                    }
                    recyclerFood.adapter = foodAdapter

                    // Tag filter (if tags available)
                    if (allTags.isNotEmpty()) {
                        val categoryAdapter = FoodCategoryAdapter(allTags) { selectedTag ->
                            val filtered = currentRestaurantFoods.filter {
                                it.tags?.contains(selectedTag) == true
                            }
                            foodAdapter.updateData(filtered)
                        }
                        recyclerCategories.adapter = categoryAdapter

                        // initial selection
                        val initTag = allTags.first()
                        val initFiltered = currentRestaurantFoods.filter {
                            it.tags?.contains(initTag) == true
                        }
                        foodAdapter.updateData(initFiltered)
                        categoryAdapter.setSelectedIndex(0)
                    } else {
                        // no tags → show all
                        recyclerCategories.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("RestaurantsDetails", "Error loading restaurant foods", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("cartCount", "onResume: ")

        if (AllScreenTracker.getInstance().isManualAppForeground) {
            SetAnalytics.getInstance()
                .setActivityEvent(
                    "RestaurantsDetailsActivity Foreground Manual", // screenName
                    this@RestaurantsDetailsActivity            // screenClass
                )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("cartCount", "onPause: ")
    }

    private fun setupDrawer() {
        cartDetail.isClickable = true
        cartDetail.isFocusable = true

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.id == R.id.drawer_container) {
                    if (supportFragmentManager.findFragmentById(R.id.drawer_container) == null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.drawer_container, CartFragment())
                            .commit()
                    }

                    SetAnalytics.getInstance()
                        .setDrawerEvent(
                            this@RestaurantsDetailsActivity, // screenClass
                            "open"             // action (open or close)
                        )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)

                SetAnalytics.getInstance()
                    .setDrawerEvent(
                        this@RestaurantsDetailsActivity, // screenClass
                        "close"             // action (open or close)
                    )
            }
        })

        cartDetail.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    // Update count
    fun updateCartCount(count: Int) {
        if (count > 0) {
            badge.text = count.toString()
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }


}