package com.twinalyze.servicedemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.model.RestaurantItem


class RestaurantAdapter (private var restaurants: List<RestaurantItem>,private val layoutId: Int,private val onItemClick: (RestaurantItem) -> Unit ) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRestaurant: ImageView = itemView.findViewById(R.id.imgRestaurant)
        val txtName: TextView = itemView.findViewById(R.id.txtRestaurantName)
        val txtRating: TextView = itemView.findViewById(R.id.txtRestaurantRating)
        val txtAddress: TextView = itemView.findViewById(R.id.txtRestaurantAddress)
        val txtType: TextView = itemView.findViewById(R.id.txtRestaurantType)
        val txtNear: TextView = itemView.findViewById(R.id.txtRestaurantNear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return RestaurantViewHolder(view)

//        val layout = if (viewType == 0) R.layout.item_restaurant else R.layout.item_restaurant1
//        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
//        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]

        Log.d("HomeFragment", "onBindViewHolder: "+restaurants[position])

        holder.txtName.text = restaurant.restaurantName
        holder.txtAddress.text = restaurant.restaurantAddress
        holder.txtRating.text = restaurant.restaurantStar ?: "N/A"
        holder.txtType.text = restaurant.restaurantType
        holder.txtNear.text = restaurant.restaurantNear

        Glide.with(holder.itemView.context)
            .load(restaurant.restaurantImage)
            .placeholder(R.drawable.ic_restaurant)
            .into(holder.imgRestaurant)

        // 👇 Click listener
        holder.itemView.setOnClickListener {
            onItemClick(restaurant)
        }
    }

    override fun getItemCount(): Int = restaurants.size

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) 0 else 1   // or based on data
    }

    fun updateData(newList: List<RestaurantItem>) {
        restaurants = newList
        notifyDataSetChanged()
    }
}