package com.twinalyze.servicedemo.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.RestaurantRepository
import com.twinalyze.servicedemo.adapter.OrderAdapter
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersListFragment : Fragment() {

    companion object {
        fun newInstance(category: String, foodListJson: String): OrdersListFragment {
            val fragment = OrdersListFragment()
            val args = Bundle()
            args.putString("category", category)
            args.putString("food_list", foodListJson)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_orders_list, container, false)

        val recyclerOrders = view.findViewById<RecyclerView>(R.id.recyclerOrder)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        recyclerOrders.layoutManager = LinearLayoutManager(requireContext())

        // Load + bind data async so UI thread light rahe
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val gson = Gson()
                val prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

                val category = requireArguments().getString("category").orEmpty()
                val foodListJson = requireArguments().getString("food_list").orEmpty()

                // Parse cart foods (if coming from last action)
                val cartFoods: List<Food> = if (foodListJson.isNotEmpty()) {
                    gson.fromJson(foodListJson, object : TypeToken<List<Food>>() {}.type)
                } else {
                    emptyList()
                }
                Log.d("OrdersList", "cartFoods: $cartFoods")

                // Load saved orders
                val savedJson = prefs.getString("orders_list", null)
                val type = object : TypeToken<MutableList<Order>>() {}.type
                val savedOrders: MutableList<Order> = if (!savedJson.isNullOrEmpty()) {
                    gson.fromJson(savedJson, type)
                } else {
                    mutableListOf()
                }

                // If new cart items exist → create one new "Pending" order (once)
                if (cartFoods.isNotEmpty()) {
                    // ensure restaurant data loaded once (from repository)
                    RestaurantRepository.preload(requireContext())
                    val restaurantList = RestaurantRepository.restaurantList

                    val selectedFoodName = cartFoods.firstOrNull()?.name.orEmpty()

                    val matchedRestaurant = withContext(Dispatchers.Default) {
                        restaurantList.firstOrNull { restaurant ->
                            restaurant.food?.any { foodItem ->
                                foodItem?.foodName.equals(selectedFoodName, ignoreCase = true)
                            } == true
                        }
                    }

                    val newOrder = Order(
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        orderId = "#ORD" + (1000..9999).random(),
                        restaurantName = matchedRestaurant?.restaurantName.orEmpty(),
                        address = matchedRestaurant?.restaurantAddress.orEmpty(),
                        status = "Pending",
                        statusColor = R.color.yellow,
                        statusBg = R.color.yellow_light,
                        items = cartFoods
                    )

                    // avoid duplicate order
                    val alreadyExists = savedOrders.any { existing ->
                        existing.restaurantName == newOrder.restaurantName &&
                                existing.items.size == newOrder.items.size &&
                                existing.items.firstOrNull()?.name ==
                                newOrder.items.firstOrNull()?.name
                    }

                    if (!alreadyExists) {
                        savedOrders.add(newOrder)
                        withContext(Dispatchers.IO) {
                            prefs.edit()
                                .putString("orders_list", gson.toJson(savedOrders))
                                .apply()
                        }
                        Log.d("OrdersList", "✅ Added new order: $newOrder")
                    } else {
                        Log.d("OrdersList", "⚠️ Order already exists, not adding again")
                    }
                }

                // Filter orders for this tab
                val filteredOrders = when (category) {
                    "Pending" -> savedOrders.filter { it.status.equals("Pending", true) }
                    "Complete" -> savedOrders.filter { it.status.equals("Complete", true) }
                    "Cancelled" -> savedOrders.filter { it.status.equals("Cancelled", true) }
                    else -> savedOrders
                }

                Log.d("OrdersList", "All orders: $savedOrders")
                Log.d("OrdersList", "Filtered [$category]: $filteredOrders")

                // Update UI
                if (filteredOrders.isEmpty()) {
                    recyclerOrders.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    recyclerOrders.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    recyclerOrders.adapter = OrderAdapter(filteredOrders)
                }
            } catch (e: Exception) {
                Log.e("OrdersList", "Error in onCreateView", e)
                recyclerOrders.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
        }

        /*val recyclerOrders = view.findViewById<RecyclerView>(R.id.recyclerOrder)
        recyclerOrders.layoutManager = LinearLayoutManager(requireContext())

        val gson = Gson()
        val prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Get foodList argument
        val category = requireArguments().getString("category").orEmpty()
        val foodListJson = requireArguments().getString("food_list").orEmpty()

        // Parse food list if available
        val cartFoods: List<Food> = if (foodListJson.isNotEmpty()) {
            gson.fromJson(foodListJson, object : TypeToken<List<Food>>() {}.type)
        } else {
            emptyList()
        }

        Log.d("cartFoods", "onCreateView: "+cartFoods)

        // Load previous saved orders (if exist)
        val savedJson = prefs.getString("orders_list", null)
        val type = object : TypeToken<MutableList<Order>>() {}.type
        val savedOrders: MutableList<Order> = if (!savedJson.isNullOrEmpty()) {
            gson.fromJson(savedJson, type)
        } else {
            mutableListOf()
        }

        // ✅ Only create new order if cartFoods is not empty
        if (cartFoods.isNotEmpty()) {
            val json = readRawText(requireContext(), R.raw.restaurantdata)
            val restaurantModel: RestaurantModel = fromJson(json)
            val restaurantList = restaurantModel.restaurant?.filterNotNull() ?: emptyList()

            val selectedFoodName = cartFoods.firstOrNull()?.name.orEmpty()
            val matchedRestaurant = restaurantList.firstOrNull { restaurant ->
                restaurant.food?.any { foodItem ->
                    foodItem?.foodName.equals(selectedFoodName, ignoreCase = true)
                } == true
            }

            val matchedRestaurantName = matchedRestaurant?.restaurantName.orEmpty()
            val matchedRestaurantAddress = matchedRestaurant?.restaurantAddress.orEmpty()

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val randomOrderId = "#ORD" + (1000..9999).random()

            val newOrder = Order(
                date = currentDate,
                orderId = randomOrderId,
                restaurantName = matchedRestaurantName,
                address = matchedRestaurantAddress,
                status = "Pending",
                statusColor = R.color.yellow,
                statusBg = R.color.yellow_light,
                items = cartFoods
            )

            // ✅ Check if same order already exists before adding
            val alreadyExists = savedOrders.any { existingOrder ->
                existingOrder.restaurantName == newOrder.restaurantName &&
                        existingOrder.items.size == newOrder.items.size &&
                        existingOrder.items.firstOrNull()?.name == newOrder.items.firstOrNull()?.name
            }

            if (!alreadyExists) {
                savedOrders.add(newOrder)
                prefs.edit().putString("orders_list", gson.toJson(savedOrders)).apply()
                Log.d("ordersAdapter", "✅ Added new order: $newOrder")
            } else {
                Log.d("ordersAdapter", "⚠️ Order already exists, not adding again")
            }

        }

        // 🔹 Filter orders by tab category (Pending / Complete / Cancelled)
        val filteredOrders = when (category) {
            "Pending" -> savedOrders.filter { it.status.equals("Pending", ignoreCase = true) }
            "Complete" -> savedOrders.filter { it.status.equals("Complete", ignoreCase = true) }
            "Cancelled" -> savedOrders.filter { it.status.equals("Cancelled", ignoreCase = true) }
            else -> savedOrders
        }

        Log.d("ordersAdapter", "onCreateView: orderss: "+savedOrders)


        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        if (filteredOrders.isEmpty()) {
            recyclerOrders.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerOrders.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val adapter = OrderAdapter(filteredOrders)
            recyclerOrders.adapter = adapter
        }*/
        return view
    }

}