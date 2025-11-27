package com.twinalyze.servicedemo.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.servicedemo.CategoryListActivity
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.RestaurantRepository
import com.twinalyze.servicedemo.RestaurantRepository.foodList
import com.twinalyze.servicedemo.RestaurantsActivity
import com.twinalyze.servicedemo.RestaurantsDetailsActivity
import com.twinalyze.servicedemo.adapter.CategoryAdapter
import com.twinalyze.servicedemo.adapter.FoodAdapter
import com.twinalyze.servicedemo.adapter.RestaurantAdapter
import com.twinalyze.servicedemo.model.CategoriesData
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var badge: TextView
    private lateinit var txtAddress: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var flatBanner: CardView
    private lateinit var cartDetail: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var prefs: SharedPreferences
    private val prefs by lazy {
        requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    private var cartCount = 0  // 🔢 badge counter


    // ✅ Step 1: Register permission launcher
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "✅ Location permission granted", Toast.LENGTH_SHORT).show()
                getUserLocation { address ->
                    txtAddress.text = address
                }

            } else {
                Toast.makeText(requireContext(), "❌ Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        try {

            badge = view.findViewById(R.id.txtCartBadge)
            txtAddress = view.findViewById(R.id.txtAddress)
            drawerLayout = view.findViewById(R.id.drawerLayout)
            cartDetail = view.findViewById(R.id.rl_cart)
            flatBanner = view.findViewById(R.id.flat_banner)

            // Drawer + cart
            setupDrawer()

            // Cart count from prefs
            cartCount = prefs.getInt("cart_count", 0)
            updateCartCount(cartCount)

            // RecyclerViews
            val recyclerCategories = view.findViewById<RecyclerView>(R.id.recyclerCategories)
            val recyclerRestaurants = view.findViewById<RecyclerView>(R.id.recyclerRestaurants)
            val recyclerFood = view.findViewById<RecyclerView>(R.id.recyclerFood)

            recyclerCategories.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerRestaurants.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerFood.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            // Temporary empty adapters (avoid NPE)
//            recyclerCategories.adapter = CategoryAdapter(mutableListOf()) {}
            recyclerRestaurants.adapter = RestaurantAdapter(emptyList(), R.layout.item_restaurant) {}
            recyclerFood.adapter = FoodAdapter(mutableListOf()) {
                Toast.makeText(requireContext(), "Add success", Toast.LENGTH_SHORT).show()
            }


            val restaurants = listOf(
                CategoriesData(
                    categoriesName = "Pizza",
                    categoriesImage = R.drawable.pizza
                ),
                CategoriesData(
                    categoriesName = "Burger",
                    categoriesImage = R.drawable.burger
                ),
                CategoriesData(
                    categoriesName = "Sandwich",
                    categoriesImage = R.drawable.sandwich
                ),
                CategoriesData(
                    categoriesName = "Chinese",
                    categoriesImage = R.drawable.chinese
                ),
                CategoriesData(
                    categoriesName = "Shake",
                    categoriesImage = R.drawable.shake
                ),
                CategoriesData(
                    categoriesName = "Cake",
                    categoriesImage = R.drawable.cake
                ),
                CategoriesData(
                    categoriesName = "Salad",
                    categoriesImage = R.drawable.salad
                )
            )
            val norm: (String?) -> String = { it?.trim()?.lowercase() ?: "" }
            val groupedByCategory: Map<String, List<FoodItem>> = foodList
                .filter { !it.foodCategories.isNullOrBlank() }
                .groupBy { norm(it.foodCategories) }

            val adapter = CategoryAdapter(mutableListOf()){ clickedItem ->
                val key = norm(clickedItem.categoriesName)
                val fullListForCategory = groupedByCategory[key].orEmpty()
                val gson = Gson()
                val jsonItems = gson.toJson(fullListForCategory)
                val intent = Intent(requireContext(), CategoryListActivity::class.java).apply {
                    putExtra("category_name", clickedItem.categoriesName)
                    putExtra("category_items_json", jsonItems)
                }
                startActivity(intent)
            }
            recyclerCategories.adapter = adapter
            adapter.setItems(restaurants)

            // Async load data (this was blocking your UI before)
            loadHomeDataAsync(
                recyclerCategories = recyclerCategories,
                recyclerRestaurants = recyclerRestaurants,
                recyclerFood = recyclerFood
            )

            // See all restaurants
            view.findViewById<TextView>(R.id.txtSeeAll).setOnClickListener {
                startActivity(Intent(requireContext(), RestaurantsActivity::class.java))
            }

            flatBanner.setOnClickListener {
                startActivity(Intent(requireContext(), RestaurantsActivity::class.java))
            }

            // Ask for location (once)
            checkLocationPermission()

        } catch (e: Exception) {
            Log.e("HomeFragment", "Init error", e)
        }

        /*try {
            badge = view.findViewById<TextView>(R.id.txtCartBadge)
            txtAddress = view.findViewById<TextView>(R.id.txtAddress)
//            rl_cart = view.findViewById<TextView>(R.id.rl_cart)
            drawerLayout = view.findViewById(R.id.drawerLayout)
            cartDetail = view.findViewById(R.id.rl_cart)

            checkLocationPermission()

//            txtAddress.text = getUserLocation()

            // make sure it’s clickable
            cartDetail.isClickable = true
            cartDetail.isFocusable = true

            drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

                override fun onDrawerOpened(drawerView: View) {
                    // drawer opened
                    if (drawerView.id == R.id.drawer_container) {
                        // e.g. load/refresh your cart fragment here
                        if (requireActivity().supportFragmentManager.findFragmentById(R.id.drawer_container) == null) {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.drawer_container, CartFragment())
                                .commit()
                        }
                    }
                }

                override fun onDrawerClosed(drawerView: View) {
                    // drawer closed
//                if (drawerView.id == R.id.drawer_container) {
//                    // optional: cleanup, persist, or refresh badge here
//                    val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
//                    updateCartCount(prefs.getInt("cart_count", 0))
//                }
                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    // called continuously while sliding (0f..1f)
                }

                override fun onDrawerStateChanged(newState: Int) {
                    // STATE_IDLE, STATE_DRAGGING, STATE_SETTLING
                }
            })

            cartDetail.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    drawerLayout.openDrawer(GravityCompat.END)
                }
            }

            // get prefs
//            prefs = requireContext().getSharedPreferences("my_prefs", MODE_PRIVATE)   // Fragment: requireContext().getSharedPreferences(...)

            cartCount = prefs.getInt("cart_count", 0)
            badge.text = cartCount.toString()
            Log.d("cartCount", "onCreateView: "+cartCount)

            // RecyclerViewCategories setup
            val recyclerCategories = view.findViewById<RecyclerView>(R.id.recyclerCategories)
            recyclerCategories.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            val json = readRawText(requireContext(), R.raw.restaurantdata)
            val restaurantModel: RestaurantModel = fromJson(json)
            val restaurantList = restaurantModel.restaurant?.filterNotNull() ?: emptyList()
            val foodList = restaurantList.flatMap { it.food?.filterNotNull() ?: emptyList() }

            val categoryNames: List<String> =
                foodList
                    .mapNotNull { it.foodCategories }          // take "Pizza", etc.
                    .flatMap { it.split(",") }                  // if ever "Pizza, Burger"
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
            Log.d("categoryNames", "onCreateView: "+categoryNames)

            val catToImage: Map<String, String?> =
                foodList
                    .filter { !it.foodCategories.isNullOrBlank() }
                    .groupBy { it.foodCategories!!.split(",").first().trim() } // if multiple, take first
                    .mapValues { (_, foods) -> foods.firstOrNull()?.foodImage }


            // 1) Build groups + a display list
            val norm: (String?) -> String = { it?.trim()?.lowercase() ?: "" }

            val groupedByCategory: Map<String, List<FoodItem>> = foodList
                .filter { !it.foodCategories.isNullOrBlank() }
                .groupBy { norm(it.foodCategories) }

// Pick a representative item per category to display
            val displayList: MutableList<FoodItem> = groupedByCategory
                .map { (_, items) ->
                    items.firstOrNull { !it.foodImage.isNullOrBlank() } ?: items.first()
                }
                .sortedBy { it.foodCategories!!.lowercase() }
                .toMutableList()

// 2) Use the display list in the adapter
            recyclerCategories.adapter = CategoryAdapter(displayList) { clickedItem ->
                // 3) On click, fetch ALL items for that category
                val key = norm(clickedItem.foodCategories)
                val fullListForCategory: List<FoodItem> = groupedByCategory[key].orEmpty()

                // --- Option A: navigate and PASS ALL items (JSON) ---
                val gson = Gson()
                val json = gson.toJson(fullListForCategory)
                val intent = Intent(requireContext(), CategoryListActivity::class.java).apply {
                    putExtra("category_name", clickedItem.foodCategories)
                    putExtra("category_items_json", json)
                }
                startActivity(intent)

            }

            // RecyclerViewRestaurants setup
            val recyclerRestaurants = view.findViewById<RecyclerView>(R.id.recyclerRestaurants)
            recyclerRestaurants.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // Make a list with two different restaurants
            val restaurants = listOf(
                RestaurantItem(
                    restaurantName = "Pizza Planet",
                    restaurantAddress = "Near Race Course, Surat",
                    restaurantStar = "4.5",
                    restaurantType = "Italian, Fast Food",
                    restaurantNear = "2 Km",
                    restaurantImage = "https://t3.ftcdn.net/jpg/03/24/73/92/360_F_324739203_keeq8udvv0P2h1MLYJ0GLSlTBagoXS48.jpg"
                ),
                RestaurantItem(
                    restaurantName = "Biryani House",
                    restaurantAddress = "City Light, Surat",
                    restaurantStar = "3.5",
                    restaurantType = "North Indian, Mughlai",
                    restaurantNear = "5 Km",
                    restaurantImage = "https://content.jdmagicbox.com/comp/surat/p5/0261px261.x261.190417192731.k5p5/catalogue/biryani-house-surat-restaurants-0dgkx6dxo0.jpg"
                ),
                RestaurantItem(
                    restaurantName = "Burger House",
                    restaurantAddress = "Adajan, Surat",
                    restaurantStar = "3.8",
                    restaurantType = "Fast Food, Burgers",
                    restaurantNear = "3 Km",
                    restaurantImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSf4yhgzX187s91NfSizODvE-m85qoDvKPVDJpSymjYsORq8CZBrPFN9INV4dhzkGAAQsQ"
                )
            )

            // 🔹 Log all data
            restaurantList.forEachIndexed { index, r ->
                Log.d("HomeFragment", "[$index] -> ${r.restaurantName}")
            }

            // 🔹 Setup adapter ONCE
            restaurantAdapter = RestaurantAdapter(restaurantList, R.layout.item_restaurant){ restaurant ->
                // 👇 Click par action

                // Example: Next page open
                val intent = Intent(requireContext(), RestaurantsDetailsActivity::class.java)
                intent.putExtra("restaurantName", restaurant.restaurantName)
                intent.putExtra("restaurantStar", restaurant.restaurantStar)
                intent.putExtra("restaurantAddress", restaurant.restaurantAddress)
                intent.putExtra("restaurantType", restaurant.restaurantType)
                intent.putExtra("restaurantNear", restaurant.restaurantNear)
                intent.putExtra("restaurantImage", restaurant.restaurantImage)
                intent.putExtra("restaurantDescription", restaurant.restaurantDescription)

                startActivity(intent)
            }
            recyclerRestaurants.adapter = restaurantAdapter

            // RecyclerViewFood setup
            val recyclerFood = view.findViewById<RecyclerView>(R.id.recyclerFood)
            recyclerFood.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


            val foodAdapter = FoodAdapter(foodList.toMutableList()) { foodItem ->
                cartCount += 1

                val gson = Gson()
                val type = object : com.google.gson.reflect.TypeToken<MutableList<Food>>() {}.type

                // read existing cart
                val cartJson = prefs.getString("cart_json", null)
                val cart: MutableList<Food> = if (cartJson.isNullOrEmpty()) mutableListOf()
                else gson.fromJson(cartJson, type)

                // add current item
                cart += Food(foodItem.foodName.orEmpty(), foodItem.foodPrice.orEmpty(), foodItem.foodImage.orEmpty(),1)

                // save once
                prefs.edit().putString("cart_json", gson.toJson(cart)).apply()

                prefs.edit().putInt("cart_count", cartCount).apply()

                updateCartCount(cartCount)  // 👈 updates txtCartBadge
                // (optional) also keep a simple list if you need items later:
                // cartItems.add(foodItem)
            }
            recyclerFood.adapter = foodAdapter

            Log.d("HomeFragment", "onCreateView foodList: " + foodList)


            val txtSeeAll = view.findViewById<TextView>(R.id.txtSeeAll)
            txtSeeAll.setOnClickListener {
                startActivity(Intent(requireContext(), RestaurantsActivity::class.java))
            }


        }catch (e: Exception) {
            Log.d("HomeFragment", "Parse/Bind error", e)
        }*/

        return view
    }


    override fun onResume() {
        super.onResume()
        // get prefs
//        prefs = requireContext().getSharedPreferences("my_prefs", MODE_PRIVATE)   // Fragment: requireContext().getSharedPreferences(...)
        cartCount = prefs.getInt("cart_count", 0)
        badge.text = cartCount.toString()
//        updateCartCount(cartCount)


//        checkLocationPermission()

        Log.d("cartCount", "onResume111: ")
    }

    private fun setupDrawer() {
        cartDetail.isClickable = true
        cartDetail.isFocusable = true

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.id == R.id.drawer_container) {
                    if (requireActivity().supportFragmentManager.findFragmentById(R.id.drawer_container) == null) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.drawer_container, CartFragment())
                            .commit()
                    }

//                    SetAnalytics.getInstance()
//                        .setDrawerEvent(
//                            this@HomeFragment, // screenClass
//                            "open"             // action (open or close)
//                        )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
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


    // endregion

    // region Data loading (async)

    private fun loadHomeDataAsync(
        recyclerCategories: RecyclerView,
        recyclerRestaurants: RecyclerView,
        recyclerFood: RecyclerView
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1) Try use preloaded data
                var restaurants = RestaurantRepository.restaurantList
                var foods = RestaurantRepository.foodList

                // 2) If not loaded (e.g. direct entry), load here async
                if (restaurants.isEmpty() || foods.isEmpty()) {
                    RestaurantRepository.preload(requireContext())
                    restaurants = RestaurantRepository.restaurantList
                    foods = RestaurantRepository.foodList
                }

                val norm: (String?) -> String = { it?.trim()?.lowercase() ?: "" }

                val groupedByCategory = withContext(Dispatchers.Default) {
                    foods
                        .filter { !it.foodCategories.isNullOrBlank() }
                        .groupBy { norm(it.foodCategories) }
                }

                val displayList = withContext(Dispatchers.Default) {
                    groupedByCategory
                        .map { (_, items) ->
                            items.firstOrNull { !it.foodImage.isNullOrBlank() } ?: items.first()
                        }
                        .sortedBy { it.foodCategories!!.lowercase() }
                        .toMutableList()
                }

                // somewhere in your Fragment/Activity after you have all foods
                val foodsByCategory: Map<String, List<FoodItem>> =
                    foods.groupBy { norm(it.foodCategories) }



                withContext(Dispatchers.Main) {
                    // Categories
                    /*recyclerCategories.adapter = CategoryAdapter(displayList) { clickedItem ->
                        val key = norm(clickedItem.foodCategories)
                        val fullListForCategory = groupedByCategory[key].orEmpty()
                        val gson = Gson()
                        val jsonItems = gson.toJson(fullListForCategory)
                        val intent = Intent(requireContext(), CategoryListActivity::class.java).apply {
                            putExtra("category_name", clickedItem.foodCategories)
                            putExtra("category_items_json", jsonItems)
                        }
                        startActivity(intent)
                    }*/
                    /*recyclerCategories.adapter = CategoryAdapter(mutableListOf()) { clickedCategory ->
                        val key = norm(clickedCategory.categoriesName)
                        val items = foodsByCategory[key].orEmpty()

                        // Option A: pass as JSON (works even if FoodItem is not Parcelable)
                        val json = Gson().toJson(items)
                        val intent = Intent(requireContext(), CategoryListActivity::class.java).apply {
                            putExtra("category_name", clickedCategory.categoriesName)
                            putExtra("category_items_json", json)
                        }
                        startActivity(intent)

                        // ---- Option B: pass as Parcelable list (only if FoodItem : Parcelable) ----
                        // val intent = Intent(requireContext(), CategoryListActivity::class.java).apply {
                        //     putExtra("category_name", clickedCategory.categoriesName)
                        //     putParcelableArrayListExtra("category_items", ArrayList(items))
                        // }
                        // startActivity(intent)
                    }*/




                    // Restaurants
                    restaurantAdapter = RestaurantAdapter(
                        restaurants,
                        R.layout.item_restaurant
                    ) { restaurant ->
                        val intent =
                            Intent(requireContext(), RestaurantsDetailsActivity::class.java).apply {
                                putExtra("restaurantName", restaurant.restaurantName)
                                putExtra("restaurantStar", restaurant.restaurantStar)
                                putExtra("restaurantAddress", restaurant.restaurantAddress)
                                putExtra("restaurantType", restaurant.restaurantType)
                                putExtra("restaurantNear", restaurant.restaurantNear)
                                putExtra("restaurantImage", restaurant.restaurantImage)
                                putExtra("restaurantDescription", restaurant.restaurantDescription)
                            }
                        startActivity(intent)
                    }
                    recyclerRestaurants.adapter = restaurantAdapter

                    // Food list
                    val foodAdapter = FoodAdapter(foods.toMutableList()) { foodItem ->
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
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading home data", e)
            }
        }
    }


    // endregion

    // already in your class
    fun updateCartCount(count: Int) {
        if (count >= 0) {
            badge.text = count.toString()
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineGranted || coarseGranted) {
                Toast.makeText(requireContext(), "✅ Location permission granted", Toast.LENGTH_SHORT).show()
                getUserLocation { address -> txtAddress.text = address }
            } else {
//                Toast.makeText(requireContext(), "❌ Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            // Permission granted → ensure Location is ON, then fetch
            if (isLocationEnabled(requireContext())) {
                getUserLocation { address -> txtAddress.text = address }
            } else {
                Toast.makeText(requireContext(), "Turn on Location (GPS)", Toast.LENGTH_LONG).show()
                // Send the user to settings
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            // Ask for both
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    private fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun getUserLocation(onAddressFetched: (String) -> Unit) {
        try {
            // Prefer current (active) reading
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            fusedLocationClient
                .getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                )
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        reverseGeocode(loc.latitude, loc.longitude, onAddressFetched)
                    } else {
                        // Fallback to cached lastLocation
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null) {
                                    reverseGeocode(last.latitude, last.longitude, onAddressFetched)
                                } else {
                                    onAddressFetched("Location not available")
                                }
                            }
                            .addOnFailureListener {
                                onAddressFetched("Failed to get last location")
                            }
                    }
                }
                .addOnFailureListener {
                    onAddressFetched("Failed to get current location")
                }
        } catch (e: SecurityException) {
            onAddressFetched("Permission denied")
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double, done: (String) -> Unit) {
        // Geocoder backend can be missing on some devices/ROMs
        if (!Geocoder.isPresent()) {
            done("Geocoder not available")
            return
        }
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                done(list[0].getAddressLine(0) ?: "${"%.5f".format(lat)}, ${"%.5f".format(lng)}")
            } else {
                done("${"%.5f".format(lat)}, ${"%.5f".format(lng)}")
            }
        } catch (e: Exception) {
            done("${"%.5f".format(lat)}, ${"%.5f".format(lng)}")
        }
    }

}
