package com.twinalyze.servicedemo.model

data class FoodOrder(
    val name: String,
    val price: String
)


data class Order (
    val date: String,
    val orderId: String,
    val restaurantName: String,
    val address: String,
    var status: String,       // "Pending", "Complete", "Cancelled"
    val statusColor: Int,     // ex: R.color.yellow
    val statusBg: Int,        // ex: R.color.yellow_light
    val items: List<Food>  // ✅ default non-null
)