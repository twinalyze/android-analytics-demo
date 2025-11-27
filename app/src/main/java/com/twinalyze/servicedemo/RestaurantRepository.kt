package com.twinalyze.servicedemo

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.twinalyze.servicedemo.model.FoodItem
import com.twinalyze.servicedemo.model.RestaurantItem
import com.twinalyze.servicedemo.model.RestaurantModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RestaurantRepository {

    @Volatile
    private var isLoaded = false

    var restaurantList: List<RestaurantItem> = emptyList()
        private set

    var foodList: List<FoodItem> = emptyList()
        private set

     suspend fun preload(context: Context) {
        if (isLoaded) return

        withContext(Dispatchers.IO) {
            val json = readRawText(context, R.raw.restaurantdata)

            val model: RestaurantModel = fromJson(json)

            val restaurants = model.restaurant?.filterNotNull() ?: emptyList()
            val foods = restaurants.flatMap { it.food?.filterNotNull() ?: emptyList() }

            restaurantList = restaurants
            foodList = foods
            isLoaded = true
        }
    }

    private fun readRawText(context: Context, @RawRes resId: Int): String =
        context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

    private inline fun <reified T> fromJson(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return Gson().fromJson(json, type)
    }
}