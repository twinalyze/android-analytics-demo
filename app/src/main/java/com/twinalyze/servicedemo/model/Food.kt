package com.twinalyze.servicedemo.model

import java.io.Serializable

data class Food(
    val name: String,
    val price: String,
    val image: String, // or String if you load from URL
    var quantity: Int = 1
): Serializable

// Row you will show in cart list
data class CartRow(
    val name: String?,
    val image: String?,
    val unitPrice: Int,
    val qty: Int,
    val totalPrice: Int
)


data class CategoriesData(
    val categoriesName: String,
    val categoriesImage: Int   // keeping your exact field name
)