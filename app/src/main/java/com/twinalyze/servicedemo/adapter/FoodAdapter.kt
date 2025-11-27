package com.twinalyze.servicedemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.twinalyze.event.Analytics
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.model.Food
import com.twinalyze.servicedemo.model.FoodItem
import org.json.JSONObject

class FoodAdapter(private var foodList: List<FoodItem>, private val onAddClick: (FoodItem) -> Unit) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {


    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val txtFoodType: TextView = itemView.findViewById(R.id.txtFoodType)
        val txtFoodRating: TextView = itemView.findViewById(R.id.txtFoodRating)
        val txtFoodName: TextView = itemView.findViewById(R.id.txtFoodName)
        val txtFoodRestaurant: TextView = itemView.findViewById(R.id.txtFoodRestaurant)
        val txtFoodPrice: TextView = itemView.findViewById(R.id.txtFoodPrice)
        val btnAdd: Button = itemView.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        holder.txtFoodName.text = food.foodName
        holder.txtFoodRestaurant.text = food.foodDescription // or pass restaurant
        holder.txtFoodPrice.text = food.foodPrice
        holder.txtFoodRating.text = food.foodStar ?: "N/A"
//        holder.txtFoodType.text = food.foodType.toString()
//        holder.txtFoodType.text = food.foodType?.removeSurrounding("[", "]") ?: ""
        holder.txtFoodType.text = food.foodType?.joinToString(", ") ?: ""

        Log.d("foodType", "onBindViewHolder: "+food.foodType)

        Glide.with(holder.itemView.context)
            .load(food.foodImage)
            .placeholder(R.drawable.ic_pizza_food)
            .into(holder.imgFood)


        holder.btnAdd.setOnClickListener {
            onAddClick(food) // ✅ notify activity
            Toast.makeText(holder.itemView.context, "${food.foodName} added!", Toast.LENGTH_SHORT).show()

            val eventProperties = JSONObject().apply {
                put("foodName", food.foodName) // Optional
                put("foodCategory", food.foodCategories)            // Optional (Double)
                put("food", food.foodType)
                put("foodPrice", food.foodPrice)           // Optional

                // put("userId", "12345")
                // put("paymentMethod", "Card")
            }

            Analytics.getInstance().setCustomEvent("AddToCart", eventProperties)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateData(newList: List<FoodItem>) {
        foodList = newList
        notifyDataSetChanged()
    }
}