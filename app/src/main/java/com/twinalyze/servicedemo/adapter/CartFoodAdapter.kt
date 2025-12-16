package com.twinalyze.servicedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.twinalyze.servicedemo.R
import com.twinalyze.servicedemo.model.Food

class CartFoodAdapter(
    private var foodList: MutableList<Food>,
    private val onTotalsChanged: (totalPrice: Int) -> Unit
) : RecyclerView.Adapter<CartFoodAdapter.FoodViewHolder>() {

    private fun notifyTotals() {
        val totalQty = foodList.sumOf { it.quantity }
        val totalPrice = foodList.sumOf { (it.price.toIntOrNull() ?: 0) * it.quantity }
        onTotalsChanged(totalPrice)
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val txtFoodName: TextView = itemView.findViewById(R.id.txtFoodName)
        val txtFoodType: TextView = itemView.findViewById(R.id.txtFoodType)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val btnPlus: ImageView = itemView.findViewById(R.id.btnPlus)
        val btnMinus: ImageView = itemView.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_cart, parent, false) // use your XML filename
        return FoodViewHolder(view)
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        // Bind data
        holder.txtFoodName.text = food.name
        holder.txtFoodType.text = "Regular"
        holder.txtPrice.text = food.price.toString()
        holder.txtQuantity.text = food.quantity.toString()

        // Image → local resource or Glide/Picasso if URL
//        holder.imgFood.setImageResource(food.image)
        val radius = holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp)


        Glide.with(holder.itemView.context)
            .load(food.image)
            .placeholder(R.drawable.ic_pizza_food)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(holder.imgFood)

        val unitPrice = food.price.toInt()

        // Handle quantity +
        holder.btnPlus.setOnClickListener {
            food.quantity++
            holder.txtQuantity.text = food.quantity.toString()
            holder.txtPrice.text = (unitPrice * food.quantity).toString()
            notifyTotals()
        }

        // Handle quantity -
        holder.btnMinus.setOnClickListener {
            if (food.quantity > 1) {
                food.quantity--
                holder.txtQuantity.text = food.quantity.toString()
                holder.txtPrice.text = (unitPrice * food.quantity).toString()
                notifyTotals()
            }
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateData(newList: List<Food>) {
        foodList = newList.toMutableList()
        notifyDataSetChanged()
        notifyTotals()
    }
    fun emitTotalsNow() {
        val total = foodList.sumOf { (it.price.toIntOrNull() ?: 0) * it.quantity }
        onTotalsChanged(total)
    }

}