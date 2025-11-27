package com.twinalyze.servicedemo.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class RestaurantItem(

	@SerializedName("restaurantStar")
	val restaurantStar: String? = null,

	@SerializedName("restaurantRated")
	val restaurantRated: String? = null,

	@SerializedName("restaurantFoodType")
//	val restaurantFoodType: String? = null,
	val restaurantFoodType: List<String?>? = null,

	@SerializedName("restaurantAddress")
	val restaurantAddress: String? = null,

	@SerializedName("restaurantName")
	val restaurantName: String? = null,

	@SerializedName("restaurantImage")
	val restaurantImage: String? = null,

	@SerializedName("restaurantType")
	val restaurantType: String? = null,

	@SerializedName("restaurantNear")
	val restaurantNear: String? = null,

	@SerializedName("restaurantDescription")
	val restaurantDescription: String? = null,

	@SerializedName("food")
	val food: List<FoodItem?>? = null
) : Parcelable

@Parcelize
data class FoodItem(

	@SerializedName("foodPrice")
	val foodPrice: String? = null,

	@SerializedName("foodName")
	val foodName: String? = null,

	@SerializedName("foodType")
//	val foodType: String? = null,
	val foodType: List<String?>? = null,

	@SerializedName("foodCategories")
	val foodCategories: String? = null,

	@SerializedName("cuisine")
	val cuisine: String? = null,

	@SerializedName("foodDescription")
	val foodDescription: String? = null,

	@SerializedName("foodImage")
	val foodImage: String? = null,

	@SerializedName("foodStar")
	val foodStar: String? = null,

	@SerializedName("tags")
	val tags: List<String?>? = null
) : Parcelable

@Parcelize
data class RestaurantModel(

	@SerializedName("restaurant")
	val restaurant: List<RestaurantItem?>? = null
) : Parcelable
