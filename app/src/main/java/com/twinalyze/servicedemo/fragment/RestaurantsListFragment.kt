package com.twinalyze.servicedemo.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.RestaurantRepository
import com.twinalyze.servicedemo.RestaurantsDetailsActivity
import com.twinalyze.servicedemo.adapter.RestaurantAdapter
import com.twinalyze.servicedemo.model.RestaurantModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantsListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        private const val ARG_CATEGORY = "category" // "veg" | "nonveg" | "top"
        fun newInstance(category: String) = RestaurantsListFragment().apply {
            arguments = bundleOf(ARG_CATEGORY to category)
        }
    }

    private val category by lazy { arguments?.getString(ARG_CATEGORY) ?: "Veg" }

    private lateinit var restaurantAdapter: RestaurantAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_restaurants_list, container, false)

        val recyclerRestaurants = view.findViewById<RecyclerView>(R.id.recyclerVeg)
        val emptyText = view.findViewById<TextView?>(R.id.tvEmpty)

        recyclerRestaurants.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Load data async from Repository (no raw JSON parse here)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ensure data loaded (if Splash already did, this is instant)
                RestaurantRepository.preload(requireContext())

                val allRestaurants = RestaurantRepository.restaurantList

                Log.d("RestaurantsList", "Total restaurants: ${allRestaurants.size}")

                val key = categoryLabel.normKey()
                Log.d("RestaurantsList", "Category key = $key")

                // filter by tab
                val filtered = withContext(Dispatchers.Default) {
                    when (key) {
                        "veg" -> allRestaurants.filter { it.restaurantFoodType.hasVeg() }
                        "non veg", "nonveg" -> allRestaurants.filter { it.restaurantFoodType.hasNonVeg() }
                        "top rated", "top" ->
                            allRestaurants.filter { it.restaurantRated?.toIntOrNull() == 1 }
                        else -> allRestaurants
                    }
                }

                Log.d("RestaurantsList", "Filtered($key) = ${filtered.size}")

                if (filtered.isEmpty()) {
                    recyclerRestaurants.isVisible = false
                    emptyText?.isVisible = true
                } else {
                    recyclerRestaurants.isVisible = true
                    emptyText?.isVisible = false

                    restaurantAdapter = RestaurantAdapter(
                        filtered,
                        R.layout.item_restaurant1
                    ) { restaurant ->
                        val intent = Intent(requireContext(), RestaurantsDetailsActivity::class.java).apply {
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
                }
            } catch (e: Exception) {
                Log.e("RestaurantsList", "Error loading restaurants", e)
                recyclerRestaurants.isVisible = false
                emptyText?.isVisible = true
            }
        }

        /*// RecyclerViewRestaurants setup
        val recyclerRestaurants = view.findViewById<RecyclerView>(R.id.recyclerVeg)
        val emptyText = view.findViewById<TextView?>(R.id.tvEmpty)       // optional empty-state TextView

        recyclerRestaurants.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        val json = readRawText(requireContext(), R.raw.restaurantdata)
        val restaurantModel: RestaurantModel = fromJson(json)

        // filter null items
        val restaurantList = restaurantModel.restaurant?.filterNotNull() ?: emptyList()

        Log.d("restaurantList", "onCreateView: "+restaurantList.toString())

        // 🔹 Log all data
        restaurantList.forEachIndexed { index, r ->
            Log.d("HomeFragment", "[$index] -> ${r.restaurantName}")
        }

        val key = categoryLabel.normKey()
        // 3) Filter by the active tab
        Log.d("restaurantList", "onCreateView: "+key)


        val filtered = when (key) {
            "veg"     -> restaurantList.filter { it.restaurantFoodType.hasVeg() }
            "non veg" -> restaurantList.filter { it.restaurantFoodType.hasNonVeg() }
            "top rated", "top" -> restaurantList.filter {
                it.restaurantRated?.toIntOrNull() == 1   // ✅ only rated = 1
            }
            else -> restaurantList
        }

        // 🔹 Setup adapter ONCE
        restaurantAdapter = RestaurantAdapter(filtered,R.layout.item_restaurant1){ restaurant ->
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

        // 5) Empty state (optional)
        val isEmpty = filtered.isEmpty()
        emptyText?.isVisible = isEmpty
        recyclerRestaurants.isVisible = !isEmpty*/

        return view
    }


    // --- in RestaurantsListFragment ---

    private val categoryLabel by lazy { arguments?.getString(ARG_CATEGORY) ?: "Veg" }

    // ---------- Helpers ----------
    private fun String.normKey() = lowercase()
        .replace("-", " ")
        .replace("\\s+".toRegex(), " ")
        .trim(' ', '.', '!', '?')

    private fun List<String?>?.hasVeg() =
        this.orEmpty().any { it?.normKey() == "veg" }

    private fun List<String?>?.hasNonVeg() =
        this.orEmpty().any { val k = it?.normKey(); k == "non veg" || k == "nonveg" }


}